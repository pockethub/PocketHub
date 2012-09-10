function getExtension(name) {
  if (!name)
    return null;

  var lastDot = name.lastIndexOf(".");
  if (lastDot == -1 || lastDot + 1 == name.length)
    return null;
  else
    return name.substring(lastDot + 1).toLowerCase();
}

function getMode(extension) {
  var mode = {};
  if (!extension)
    return mode;

  switch (extension) {
  case "cc":
  case "h":
    mode.mode = "text/x-csrc";
    mode.file = "clike";
    break;
  case "clj":
    mode.mode = "text/x-clojure";
    mode.file = "clojure";
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
  case "sass":
  case "scss":
    mode.mode = "text/css";
    mode.file = "css";
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
    mode.file = "htmlmixed";
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
  case "json":
    mode.mode = "text/javascript";
    mode.file = "javascript";
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
  case "prefs":
    mode.mode = "text/x-properties";
    mode.file = "properties";
    break;
  case "py":
    mode.mode = "text/x-python";
    mode.file = "python";
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
  case "project":
  case "classpath":
  case "xml":
    mode.mode = "application/xml";
    mode.file = "xml";
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

function loadImage(type, content) {
  var img = document.createElement("img");
  img.setAttribute("src", "data:image/" + type + ";base64," + content);
  document.body.appendChild(img);
}

function loadEditor() {
  var name = SourceEditor.getName();
  var extension = getExtension(name);
  if ("png" == extension || "gif" == extension) {
    loadImage(extension, SourceEditor.getRawContent());
    return;
  } else if ("jpg" == extension || "jpeg" == extension) {
    loadImage("jpeg", SourceEditor.getRawContent());
    return;
  }

  CodeMirror.modeURL = "mode/%N/%N.js";

  var config = {};
  config.value = SourceEditor.getContent();
  config.readOnly = "nocursor";
  config.lineNumbers = true;
  config.autofocus = false;
  config.lineWrapping = SourceEditor.getWrap();
  var editor = CodeMirror(document.body, config);

  var mode = getMode(extension);
  if (mode.mode)
    editor.setOption("mode", mode.mode);
  if (mode.file)
    CodeMirror.autoLoadMode(editor, mode.file);

  if (!config.lineWrapping)
    updateWidth();
}