"""Offline regression tests for the backport coordinator and its trust boundaries."""

import importlib.util
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location("backport", Path(__file__).parents[1] / "backport-commit.py")
bp = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(bp)

POM = '''<project xmlns="http://maven.apache.org/POM/4.0.0">
<properties><revision>0.1.40-SNAPSHOT</revision></properties>
<modules><module>microsphere-spring-compatible</module></modules></project>'''


def response(arguments, **overrides):
    result = {"choices": [{"finish_reason": "tool_calls", "message": {"tool_calls": [
        {"id": "repair-1", "type": "function", "function": {
            "name": "submit_backport_repair", "arguments": json.dumps(arguments)}}]}}]}
    result["choices"][0].update(overrides)
    return result


def repair(edits):
    return dict(status="repair", edits=edits, summary="Adapt target", adaptations=[], blocker="")


class RepositoryTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.repo = self.root / "repo"
        self.state_dir = self.root / "state"
        self.repo.mkdir()
        self.git("init", "-b", "main")
        self.git("config", "user.name", "Backport Test")
        self.git("config", "user.email", "backport@example.invalid")
        self.git("config", "core.autocrlf", "false")
        self.write("pom.xml", POM)
        self.write("microsphere-spring-parent/pom.xml", POM)
        self.write("module/src/test/java/OldTest.java", "import org.junit.Test;\npublic class OldTest {}\n")
        self.write(".gitignore", "target/\n")
        self.base = self.commit("Baseline")
        self.git("update-ref", "refs/remotes/origin/dev-1.x", self.base)

    def tearDown(self):
        self.temp.cleanup()

    def git(self, *args):
        result = subprocess.run(["git", *args], cwd=self.repo, capture_output=True, check=True)
        return result.stdout.decode().strip()

    def write(self, name, content):
        file = self.repo / name
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(content, encoding="utf-8")

    def commit(self, message):
        self.git("add", "-A")
        self.git("commit", "-m", message)
        sha = self.git("rev-parse", "HEAD")
        self.git("update-ref", "refs/remotes/origin/main", sha)
        return sha

    def select_and_prepare(self, sha=""):
        bp.select(self.repo, self.state_dir, self.base, sha, offline=True)
        self.git("checkout", "--detach", self.base)
        bp.prepare(self.repo, self.state_dir)
        return bp.load(self.state_dir)

    def test_discovery_includes_commits_from_merged_branch(self):
        self.git("checkout", "-b", "feature")
        self.write("module/src/main/java/New.java", "class New {}\n")
        feature = self.commit("Add feature")
        self.git("checkout", "main")
        self.git("merge", "--no-ff", "feature", "-m", "Merge feature")
        self.git("update-ref", "refs/remotes/origin/main", self.git("rev-parse", "HEAD"))
        bp.select(self.repo, self.state_dir, self.base, "", offline=True)
        self.assertEqual(feature, bp.load(self.state_dir)["source"])

    def test_open_pr_does_not_defer_later_commit_touching_same_file(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        first = self.commit("Ignore Sonar")
        self.write(".gitignore", "target/\n.sonar/\n.idea/\n")
        second = self.commit("Ignore editor files")
        pr = dict(number=1, state="open", body=bp.marker(first), merged_at=None,
                  head=dict(repo=dict(full_name="owner/repo"), ref=bp.branch_name(first)))
        with patch.object(bp, "GitHub") as factory:
            api = factory.return_value
            api.repository = "owner/repo"
            api.pages.side_effect = [[pr], []]
            bp.select(self.repo, self.state_dir, self.base, "", offline=False)
        self.assertEqual(second, bp.load(self.state_dir)["source"])

    def test_auto_filters_and_manual_docs_override(self):
        self.write("docs/new.md", "new document")
        sha = self.commit("Document change")
        self.assertFalse(bp.automatic_eligible(self.repo, sha))
        state = self.select_and_prepare(sha)
        self.assertEqual([], state["excluded"])
        self.assertTrue((self.repo / "docs/new.md").exists())
        bp.check_candidate(self.repo, state)

    def test_mixed_clean_addition_omits_absent_document(self):
        self.write("module/src/main/java/New.java", "class New {}\n")
        self.write("docs/new.md", "main only")
        self.commit("Source and documentation")
        state = self.select_and_prepare()
        self.assertEqual(["docs/new.md"], state["excluded"])
        self.assertFalse((self.repo / "docs/new.md").exists())
        bp.check_candidate(self.repo, state)

    def test_omitting_document_rename_keeps_existing_target_document(self):
        self.write("docs/old.md", "existing target document\n")
        self.base = self.commit("Target documentation baseline")
        self.git("update-ref", "refs/remotes/origin/dev-1.x", self.base)
        self.git("mv", "docs/old.md", "docs/new.md")
        self.write("module/src/main/java/New.java", "class New {}\n")
        self.commit("Source change and document rename")
        state = self.select_and_prepare()
        self.assertEqual("existing target document\n", (self.repo / "docs/old.md").read_text())
        self.assertFalse((self.repo / "docs/new.md").exists())
        bp.check_candidate(self.repo, state)

    def test_invalid_baseline_fails_instead_of_processing_all_history(self):
        with self.assertRaises(bp.OperationalError):
            bp.select(self.repo, self.state_dir, "main; touch BAD", "", offline=True)

    def test_preserve_revision_even_when_maven_override_would_hide_it(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        state = self.select_and_prepare()
        state["allowed"].append("pom.xml")
        self.write("pom.xml", POM.replace("0.1.40-SNAPSHOT", "0.2.40-SNAPSHOT"))
        with self.assertRaisesRegex(bp.InvalidRepair, "Protected target POM"):
            bp.check_candidate(self.repo, state)

    def test_entire_edit_batch_checked_before_any_writes(self):
        path = "module/src/test/java/OldTest.java"
        original = (self.repo / path).read_bytes()
        state = {"allowed": [path]}
        proposed = bp.decode_repair(response(repair([dict(path=path, operation="write", content="replacement"),
                                                     dict(path="../outside", operation="write", content="bad")])))
        with self.assertRaises(bp.InvalidRepair):
            bp.apply_edits(self.repo, state, proposed["edits"])
        self.assertEqual(original, (self.repo / path).read_bytes())

    def test_target_junit4_survives_rename_repair(self):
        old = "module/src/test/java/OldTest.java"
        new = "module/src/test/java/NewTest.java"
        self.git("mv", old, new)
        self.write(new, "import org.junit.jupiter.api.Test;\nclass NewTest {}\n")
        self.commit("Rename test")
        state = self.select_and_prepare()
        with self.assertRaises(bp.InvalidRepair):
            bp.check_candidate(self.repo, state)
        proposed = bp.decode_repair(response(repair([dict(path=new, operation="write",
                    content="import org.junit.Test;\npublic class NewTest {}\n")])))
        bp.apply_edits(self.repo, state, proposed["edits"])
        bp.check_candidate(self.repo, state)

    def test_clean_validated_candidate_never_calls_model_or_retests(self):
        self.state_dir.mkdir()
        bp.save(self.state_dir, {"status": "validated", "attempts": 0})
        with patch.object(bp, "request_json") as api, patch.object(bp, "maven_validate") as maven:
            bp.repair_candidate(self.repo, self.state_dir)
            bp.validate(self.repo, self.state_dir)
        api.assert_not_called()
        maven.assert_not_called()

    def test_orphaned_branch_is_revalidated_without_merging(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        sha = self.commit("Ignore Sonar")
        self.select_and_prepare()
        candidate = self.git("rev-parse", "HEAD")
        self.git("update-ref", "refs/remotes/origin/" + bp.branch_name(sha), candidate)
        self.git("checkout", "--detach", self.base)
        self.git("branch", "-D", bp.branch_name(sha))
        bp.select(self.repo, self.state_dir, self.base, "", offline=True)
        bp.prepare(self.repo, self.state_dir)
        self.assertEqual(candidate, self.git("rev-parse", "HEAD"))
        with patch.object(bp, "maven_validate", return_value="") as maven:
            bp.validate(self.repo, self.state_dir)
        maven.assert_called_once()
        bp.finish(self.repo, self.state_dir)
        self.assertEqual(candidate, bp.load(self.state_dir)["candidate"])

    def test_diverged_orphaned_branch_is_recorded_as_blocked(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        sha = self.commit("Ignore Sonar")
        self.select_and_prepare()
        candidate = self.git("rev-parse", "HEAD")
        self.git("update-ref", "refs/remotes/origin/" + bp.branch_name(sha), candidate)
        self.git("checkout", "--detach", self.base)
        self.git("branch", "-D", bp.branch_name(sha))
        self.write("target-only.txt", "target advance")
        self.git("add", "target-only.txt")
        self.git("commit", "-m", "Advance target")
        new_base = self.git("rev-parse", "HEAD")
        self.git("update-ref", "refs/remotes/origin/dev-1.x", new_base)
        bp.select(self.repo, self.state_dir, self.base, "", offline=True)
        state = bp.load(self.state_dir)
        self.assertEqual(candidate, state["recovery"])
        bp.prepare(self.repo, self.state_dir)
        state = bp.load(self.state_dir)
        self.assertEqual("blocked", state["status"])
        self.assertIn("maintainer", state["diagnostics"])
        self.assertEqual(new_base, self.git("rev-parse", "HEAD"))
        self.assertEqual(candidate, self.git("rev-parse", "origin/" + state["branch"]))
        with patch.object(bp, "request_json") as model, patch.object(bp, "maven_validate") as maven:
            bp.validate(self.repo, self.state_dir)
            bp.repair_candidate(self.repo, self.state_dir)
        model.assert_not_called()
        maven.assert_not_called()
        bp.finish(self.repo, self.state_dir)
        with patch.object(bp, "GitHub") as factory:
            api = factory.return_value
            api.repository = "owner/repo"
            api.pages.return_value = []
            bp.publish(self.repo, self.state_dir)
        self.assertEqual(1, api.call.call_count)
        endpoint, payload = api.call.call_args.args
        self.assertEqual("/issues", endpoint)
        self.assertIn(bp.marker(sha), payload["body"])
        self.assertFalse((self.state_dir / "changes.bundle").exists())

    def test_blocker_detection_uses_metadata_not_title_or_bare_sha(self):
        sha = "a" * 40
        issue = dict(title="Backport unrelated", body=bp.marker(sha), author_association="OWNER")
        self.assertEqual("blocked", bp.existing_state(sha, "o/r", [], [issue], "")[0])
        issue["body"] = "mentions " + sha
        self.assertEqual("pending", bp.existing_state(sha, "o/r", [], [issue], "")[0])

    def test_prepare_does_not_change_repository_git_identity(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        self.select_and_prepare()
        self.assertEqual("Backport Test", self.git("config", "--local", "user.name"))

    def test_repairs_use_fresh_tool_requests_with_current_files_and_diagnostics(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        state = self.select_and_prepare()
        state.update(status="needs_repair", diagnostics="First diagnostic")
        bp.save(self.state_dir, state)
        edited = "target/\n.sonar/\n.cache/\n"
        answer = response(repair([dict(path=".gitignore", operation="write", content=edited)]))
        with patch.dict(os.environ, {"DEEPSEEK_API_KEY": "test-key"}), patch.object(bp, "request_json", return_value=answer) as api:
            bp.repair_candidate(self.repo, self.state_dir)
            state = bp.load(self.state_dir)
            state["diagnostics"] = "Second diagnostic"
            bp.save(self.state_dir, state)
            bp.repair_candidate(self.repo, self.state_dir)
        payload = api.call_args.args[2]
        self.assertEqual("disabled", payload["thinking"]["type"])
        self.assertTrue(payload["tools"][0]["function"]["strict"])
        for call in api.call_args_list:
            self.assertEqual(["system", "user"], [m["role"] for m in call.args[2]["messages"]])
        context = json.loads(payload["messages"][1]["content"])
        self.assertEqual("Second diagnostic", context["diagnostics"])
        self.assertEqual(edited, context["files"][0]["candidate"])
        self.assertEqual(2, context["attempt"])
        state = bp.load(self.state_dir)
        self.assertNotIn("conversation", state)
        self.assertNotIn("last_tool_id", state)

    def test_repair_preserves_compiler_platform_and_test_execution_settings(self):
        self.write("pom.xml", POM.replace("</properties>", "<test.fixture.version>1</test.fixture.version></properties>"))
        sha = self.commit("Change POM")
        state = self.select_and_prepare(sha)
        for name, value in (("skipTests", "true"), ("maven.test.skip", "true"),
                            ("maven.test.failure.ignore", "true"), ("test", "SingleTest"),
                            ("java.version", "17"), ("spring-framework.version", "6.0.0"),
                            ("maven-surefire-plugin.version", "2.0"), ("surefire.excludes", "**/*"),
                            ("maven.main.skip", "true"), ("maven.compiler.failOnError", "false"),
                            ("maven.compiler.executable", "/another/jdk/javac")):
            with self.subTest(property=name):
                self.write("pom.xml", POM.replace("</properties>", f"<{name}>{value}</{name}></properties>"))
                with self.assertRaisesRegex(bp.InvalidRepair, "Protected target POM"):
                    bp.check_candidate(self.repo, state)

    def test_manual_pom_change_allows_unrelated_test_and_java_dependency_properties(self):
        self.write("pom.xml", POM.replace("</properties>", "<test.fixture.version>2</test.fixture.version>"
                                         "<microsphere-java.version>0.3.20</microsphere-java.version></properties>"))
        sha = self.commit("Update compatible dependencies")
        state = self.select_and_prepare(sha)
        bp.check_candidate(self.repo, state)

    def test_rejected_repairs_are_bounded_and_do_not_record_unapplied_adaptations(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        state = self.select_and_prepare()
        state.update(status="needs_repair", diagnostics="Candidate failure")
        bp.save(self.state_dir, state)
        invalid = repair([dict(path="../outside", operation="write", content="bad")])
        invalid["adaptations"] = ["Never applied"]
        with patch.dict(os.environ, {"DEEPSEEK_API_KEY": "test-key"}), \
                patch.object(bp, "request_json", return_value=response(invalid)) as api:
            for _ in range(4):
                bp.repair_candidate(self.repo, self.state_dir)
        state = bp.load(self.state_dir)
        self.assertEqual(3, api.call_count)
        self.assertEqual(3, state["attempts"])
        self.assertEqual([], state["adaptations"])
        self.assertIn("Rejected repair", state["diagnostics"])
        bp.finish(self.repo, self.state_dir)
        self.assertEqual("blocked", bp.load(self.state_dir)["status"])

    def test_passing_candidate_skips_baseline(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        self.select_and_prepare()
        with patch.object(bp, "maven_validate", return_value="") as maven, \
                patch.object(bp, "validate_baseline") as baseline:
            bp.validate(self.repo, self.state_dir)
        maven.assert_called_once()
        baseline.assert_not_called()
        self.assertEqual("validated", bp.load(self.state_dir)["status"])

    def test_candidate_failure_diagnoses_baseline_once_without_changing_candidate(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        self.select_and_prepare()
        candidate = self.git("rev-parse", "HEAD")
        self.write(".gitignore", "target/\n.sonar/\n.current-edit/\n")
        failures = "Could not find artifact introduced:by-source:jar:1"
        calls = []

        def maven(repo, state_dir, prefix):
            calls.append(prefix)
            if prefix == "baseline":
                self.assertEqual(self.base, bp.git(repo, "rev-parse", "HEAD").stdout.strip())
                self.assertEqual("target/\n", (repo / ".gitignore").read_text())
                return ""
            return failures

        with patch.object(bp, "maven_validate", side_effect=maven):
            bp.validate(self.repo, self.state_dir)
            state = bp.load(self.state_dir)
            state["attempts"] = 1
            bp.save(self.state_dir, state)
            bp.validate(self.repo, self.state_dir)
        self.assertEqual(["attempt-0", "baseline", "attempt-1"], calls)
        state = bp.load(self.state_dir)
        self.assertEqual("needs_repair", state["status"])
        self.assertEqual(failures, state["diagnostics"])
        self.assertTrue(state["baseline_validated"])
        self.assertEqual(candidate, self.git("rev-parse", "HEAD"))
        self.assertIn(".current-edit/", (self.repo / ".gitignore").read_text())
        self.assertEqual(1, self.git("worktree", "list", "--porcelain").count("worktree "))
        self.assertEqual([], list(self.state_dir.glob("baseline-*")))

    def test_failing_baseline_stops_run_and_cleans_up_diagnostic_checkout(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        self.select_and_prepare()
        with patch.object(bp, "maven_validate", return_value="Existing target failure"), \
                patch.object(bp, "request_json") as model:
            with self.assertRaisesRegex(bp.OperationalError, "baseline also fails"):
                bp.validate(self.repo, self.state_dir)
        model.assert_not_called()
        self.assertEqual(0, bp.load(self.state_dir)["attempts"])
        self.assertEqual("prepared", bp.load(self.state_dir)["status"])
        self.assertEqual(1, self.git("worktree", "list", "--porcelain").count("worktree "))
        self.assertEqual([], list(self.state_dir.glob("baseline-*")))

    def test_compatibility_failure_goes_directly_to_repair_without_baseline_builds(self):
        self.write("module/src/test/java/OldTest.java", "import org.junit.jupiter.api.Test;\nclass OldTest {}\n")
        self.commit("Update test framework")
        self.select_and_prepare()
        with patch.object(bp, "maven_validate") as maven, patch.object(bp, "validate_baseline") as baseline:
            bp.validate(self.repo, self.state_dir)
        maven.assert_not_called()
        baseline.assert_not_called()
        self.assertEqual("needs_repair", bp.load(self.state_dir)["status"])

    def test_maven_dependency_and_application_failures_remain_repairable(self):
        for message in ("Could not find artifact new:dependency:jar:1",
                        "Non-resolvable parent POM: Could not resolve artifact new:parent:pom:1",
                        "ConnectionTest.testRequest: Connection reset"):
            with self.subTest(message=message):
                version = subprocess.CompletedProcess([], 0, 'openjdk version "1.8.0_504"')
                failed = subprocess.CompletedProcess([], 1, "[ERROR] " + message)
                with patch.object(bp.shutil, "which", return_value="mvn"), \
                        patch.object(bp, "run", side_effect=[version, failed, failed]) as command:
                    errors = bp.maven_validate(self.repo, self.state_dir, "candidate")
                self.assertIn(message, errors)
                self.assertEqual(3, command.call_count)

    def test_maven_explicit_transport_and_auth_failures_stop_run(self):
        for message in ("Could not transfer artifact: Unknown host repo.example",
                        "Could not transfer artifact: status code: 401, Unauthorized"):
            with self.subTest(message=message):
                version = subprocess.CompletedProcess([], 0, 'openjdk version "1.8.0_504"')
                failed = subprocess.CompletedProcess([], 1, "[ERROR] " + message)
                with patch.object(bp.shutil, "which", return_value="mvn"), \
                        patch.object(bp, "run", side_effect=[version, failed]):
                    with self.assertRaisesRegex(bp.OperationalError, "transport/authentication"):
                        bp.maven_validate(self.repo, self.state_dir, "candidate")

    def test_publish_pushes_validated_bundle_and_creates_only_target_pr(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        state = self.select_and_prepare()
        state.update(status="validated", validated_diff=bp.candidate_digest(self.repo, state["base"]))
        bp.save(self.state_dir, state)
        bp.finish(self.repo, self.state_dir)
        remote = self.root / "remote.git"
        self.git("init", "--bare", str(remote))
        self.git("remote", "add", "origin", str(remote))
        expected = {"GITHUB_ACTIONS": "true", "EXPECTED_SOURCE_SHA": state["source"],
                    "EXPECTED_TARGET_SHA": state["base"], "EXPECTED_BASELINE_SHA": state["baseline"],
                    "EXPECTED_MANUAL": "false"}
        with patch.object(bp, "GitHub") as factory, patch.dict(os.environ, expected):
            api = factory.return_value
            api.repository = "owner/repository"
            api.pages.return_value = []
            bp.publish(self.repo, self.state_dir)
            self.assertEqual(1, api.call.call_count)
            endpoint, payload = api.call.call_args.args
            self.assertEqual("/pulls", endpoint)
            self.assertEqual("dev-1.x", payload["base"])
            self.assertEqual(state["branch"], payload["head"])
        self.assertIn(bp.load(self.state_dir)["candidate"], self.git("ls-remote", "origin", "refs/heads/" + state["branch"]))

    def test_publish_rejects_changed_coordinator_identity(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        self.select_and_prepare()
        with patch.object(bp, "GitHub"), patch.dict(os.environ, {"EXPECTED_SOURCE_SHA": "0" * 40}):
            with self.assertRaisesRegex(bp.OperationalError, "coordinator output"):
                bp.publish(self.repo, self.state_dir)

    def test_finish_rejects_edits_after_validation(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        self.commit("Ignore Sonar")
        state = self.select_and_prepare()
        state.update(status="validated", validated_diff=bp.candidate_digest(self.repo, state["base"]))
        bp.save(self.state_dir, state)
        self.write(".gitignore", "target/\n.sonar/\nunvalidated-change\n")
        with self.assertRaisesRegex(bp.OperationalError, "passed validation"):
            bp.finish(self.repo, self.state_dir)

    def test_conflicted_candidate_can_finish_as_one_backport_commit(self):
        self.write(".gitignore", "target/\n.sonar/\n")
        sha = self.commit("Ignore Sonar")
        state = self.select_and_prepare()
        state["status"] = "validated"
        state["validated_diff"] = bp.candidate_digest(self.repo, state["base"])
        bp.save(self.state_dir, state)
        bp.finish(self.repo, self.state_dir)
        state = bp.load(self.state_dir)
        self.assertEqual("ready", state["status"])
        self.assertTrue(bp.ancestor(self.repo, self.base, state["candidate"]))
        self.assertTrue(bp.has_source_marker(self.git("log", "--format=%B"), sha))
        self.assertTrue((self.state_dir / "changes.bundle").is_file())


class ProtocolTest(unittest.TestCase):
    def test_reject_truncated_and_unknown_tool(self):
        valid = repair([dict(path=".gitignore", operation="write", content=".sonar/\n")])
        with self.assertRaises(bp.InvalidRepair):
            bp.decode_repair(response(valid, finish_reason="length"))
        bad = response(valid)
        bad["choices"][0]["message"]["tool_calls"][0]["function"]["name"] = "run_shell"
        with self.assertRaises(bp.InvalidRepair):
            bp.decode_repair(bad)

    def test_reject_blocked_response_with_edits(self):
        data = repair([dict(path=".gitignore", operation="write", content="bad")])
        data.update(status="blocked", blocker="Cannot repair")
        with self.assertRaises(bp.InvalidRepair):
            bp.decode_repair(response(data))

    def test_record_states_and_untrusted_blocker(self):
        sha = "a" * 40
        repo = "owner/repository"
        pr = dict(state="open", body=bp.marker(sha), merged_at=None,
                  head=dict(repo=dict(full_name=repo), ref=bp.branch_name(sha)))
        self.assertEqual("proposed", bp.existing_state(sha, repo, [pr], [], "")[0])
        pr["state"] = "closed"
        self.assertEqual("declined", bp.existing_state(sha, repo, [pr], [], "")[0])
        pr["merged_at"] = "2026-10-24"
        self.assertEqual("applied", bp.existing_state(sha, repo, [pr], [], "")[0])
        issue = dict(body=bp.marker(sha), author_association="NONE")
        self.assertEqual("pending", bp.existing_state(sha, repo, [], [issue], "")[0])
        issue["author_association"] = "OWNER"
        self.assertEqual("blocked", bp.existing_state(sha, repo, [], [issue], "")[0])

    def test_subprocess_environment_drops_credentials(self):
        completed = subprocess.CompletedProcess([], 0, stdout=b"ok")
        with patch.dict(os.environ, {"DEEPSEEK_API_KEY": "test-key", "GH_TOKEN": "test-token"}), \
                patch.object(subprocess, "run", return_value=completed) as child:
            bp.run(["mvn", "test"], Path.cwd())
        self.assertNotIn("DEEPSEEK_API_KEY", child.call_args.kwargs["env"])
        self.assertNotIn("GH_TOKEN", child.call_args.kwargs["env"])

    def test_applied_history_takes_precedence_over_declined_pr(self):
        sha = "a" * 40
        pr = dict(state="closed", body=bp.marker(sha), merged_at=None,
                  head=dict(repo=dict(full_name="owner/repo"), ref=bp.branch_name(sha)))
        status, _ = bp.existing_state(sha, "owner/repo", [pr], [], f"Backport {sha}: already merged")
        self.assertEqual("applied", status)


if __name__ == "__main__":
    unittest.main()
