import { spawnSync } from "child_process";
import { defineConfig } from "vite";

function isDev() {
  return process.env.NODE_ENV != "production";
}

function printSbtTask(task) {
  const args = ["--error", "--batch", `print ${task}`];
  const options = {
    stdio: [
      "pipe", // StdIn.
      "pipe", // StdOut.
      "inherit", // StdErr.
    ],
  };
  const result = process.platform === 'win32'
    ? spawnSync("sbt.bat", args.map(x => `"${x}"`), {shell: true, ...options})
    : spawnSync("sbt", args, options);

  if (result.error)
    throw result.error;
  if (result.status !== 0)
    throw new Error(`sbt process failed with exit code ${result.status}`);
  const results = result.stdout.toString('utf8').replace(/(\r\n|\n|\r)/gm, "");
  console.log(`got from SBT trimmed: "${results}"`);
  return results;
}

const replacementForPublic = isDev()
  ? printSbtTask("fastLinkJSOutput")
  : printSbtTask("fullLinkJSOutput");

export default defineConfig({
  resolve: {
    preserveSymlinks: true,
    alias: [
      {
        find: "/generated-public",
        replacement: replacementForPublic,
      },
    ],
  },
});
