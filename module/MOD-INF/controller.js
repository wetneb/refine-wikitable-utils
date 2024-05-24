var html = "text/html";
var encoding = "UTF-8";
var ClientSideResourceManager = Packages.com.google.refine.ClientSideResourceManager;

function init() {
  // register the exporter in the backend
  var ER = Packages.com.google.refine.exporters.ExporterRegistry;
  ER.registerExporter("wikitable", new Packages.eu.delpeuch.antonin.refine.wikitable.WikitextExporter());

  // register it in the frontend
  ClientSideResourceManager.addPaths(
    "project/scripts",
    module,
    [
      "scripts/project-injection.js"
    ]
  );
}

