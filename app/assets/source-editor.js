function getMode(name) {
  var mode = {};
  if (!name)
    return mode;

  var lastDot = name.lastIndexOf(".")
  if (lastDot == -1 || lastDot + 1 == name.length)
    return mode;

  var extension = name.substring(lastDot + 1).toLowerCase();
  switch (extension) {
  case "cc":
  case "h":
    mode.mode = "text/x-csrc";
    mode.file = "clike";
    break;
  case "clj":
    mode.mode = "text/x-clojure";
    mode.file = extension;
    break;
  case "coffee":
    mode.mode = "text/x-coffeescript";
    mode.file = "coffeescript";
    break;
  case "cpp":
    mode.mode = "text/x-c++src";
    mode.file = "clike";
    break;
  case "cs":
    mode.mode = "text/x-csharp";
    mode.file = "clike";
    break;
  case "css":
    mode.mode = "text/css";
    mode.file = extension;
    break;
  case "erl":
    mode.mode = "text/x-erlang";
    break;
  case "hs":
  case "hsc":
    mode.mode = "text/x-haskell";
    break;
  case "html":
    mode.mode = "text/html";
    break;
  case "ini":
    mode.mode = "text/x-ini";
    mode.file = extension;
    break;
  case "java":
    mode.mode = "text/x-java";
    mode.file = "clike";
    break;
  case "js":
    mode.mode = "text/javascript";
    mode.file = extension;
    break;
  case "json":
    mode.mode = "application/json";
    mode.file = extension;
    break;
  case "md":
  case "markdown":
    mode.mode = "gfm";
    mode.file = "gfm";
    break;
  case "pl":
    mode.mode = "text/x-perl";
    mode.file = extension;
    break;
  case "py":
    mode.mode = "text/x-python";
    mode.file = extension;
    break;
  case "r":
    mode.mode = "text/x-rsrc";
    mode.file = extension;
    break;
  case "rb":
    mode.mode = "text/x-ruby";
    mode.file = extension;
    break;
  case "sh":
  case "zsh":
    mode.mode = "text/x-sh";
    mode.file = "shell";
    break;
  case "sql":
    mode.mode = "text/x-mysql";
    mode.file = "mysql";
    break;
  case "xq":
  case "xqy":
  case "xquery":
    mode.mode = "application/xquery";
    mode.file = "xquery";
    break;
  case "xml":
    mode.mode = "application/xml";
    mode.file = extension;
    break;
  case "yml":
    mode.mode = "text/x-yaml";
    mode.file = "yaml";
    break;
  default:
    mode.mode = "text/x-" + extension;
    mode.file = extension;
  }
  return mode;
}

function updateWidth() {
  var lines = document.getElementsByClassName("CodeMirror-lines")[0];
  if (lines) {
    var root = document.getElementsByClassName("CodeMirror")[0];
    if (root && lines.scrollWidth > lines.clientWidth)
      root.style.width = lines.scrollWidth + "px";
  }
}

function loadEditor() {
  CodeMirror.modeURL = "mode/%N/%N.js";

  var config = {};
  config.value = SourceEditor.getContent();
  config.readOnly = "nocursor";
  config.lineNumbers = true;
  config.autofocus = false;
  config.lineWrapping = SourceEditor.getWrap();
  var editor = CodeMirror(document.body, config);

  var mode = getMode(SourceEditor.getName());
  if (mode.mode)
    editor.setOption("mode", mode.mode);
  if (mode.file)
    CodeMirror.autoLoadMode(editor, mode.file);

  if (!config.lineWrapping)
    updateWidth();
}