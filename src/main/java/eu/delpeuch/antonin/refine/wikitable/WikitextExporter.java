package eu.delpeuch.antonin.refine.wikitable;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.sweble.wikitext.parser.ParserConfig;
import org.sweble.wikitext.parser.nodes.WikitextNodeFactory;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtTable;
import org.sweble.wikitext.parser.nodes.WtTableCell;
import org.sweble.wikitext.parser.utils.SimpleParserConfig;
import org.sweble.wikitext.parser.utils.WtPrettyPrinter;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.refine.browsing.Engine;
import com.google.refine.exporters.CustomizableTabularExporterUtilities;
import com.google.refine.exporters.TabularSerializer;
import com.google.refine.exporters.TabularSerializer.CellData;
import com.google.refine.exporters.WriterExporter;
import com.google.refine.model.Project;

/**
 * Exports an OpenRefine project to a wiki table.
 * 
 * See https://www.mediawiki.org/wiki/Help:Tables for the syntax.
 */
public class WikitextExporter implements WriterExporter {
    
    Pattern escapedCharacters = Pattern.compile("[\\|!]+");
    ParserConfig parserConfig = new SimpleParserConfig();
    WikitextNodeFactory f = parserConfig.getNodeFactory();

    @Override
    public String getContentType() {
        return "text/wiki";
    }

    @Override
    public void export(Project project, Properties options, Engine engine, Writer writer) throws IOException {
        
        List<WtNode> columnHeaders = project.columnModel.columns.stream()
                .map(column -> f.th(f.emptyAttrs(), f.body(f.list(
                        text(column.getName()),
                        f.newline("\n")
                        ))))
                .collect(Collectors.toList());
        
        List<WtNode> rows = new ArrayList<>();
        TabularSerializer serializer = new TabularSerializer() {

            @Override
            public void startFile(JsonNode options) {
                // nothing to do
            }

            @Override
            public void endFile() {
                // nothing to do
                
            }

            @Override
            public void addRow(List<CellData> cells, boolean isHeader) {
                List<WtNode> renderedCells = new ArrayList<>();
                for (CellData cellData : cells) {
                    renderedCells.add(translateCell(cellData));
                }
                rows.add(f.tr(f.emptyAttrs(), f.body(f.list(renderedCells))));
            }
        
        };

        options.setProperty("options", "{\"outputColumnHeaders\":false}"); // already included separately
        CustomizableTabularExporterUtilities.exportRows(
                project, engine, options, serializer);
        
        List<WtNode> tableBody = new ArrayList<>(columnHeaders);
        tableBody.addAll(rows);
        
        WtTable table = f.table(f.emptyAttrs(), f.body(f.list(tableBody)));
        
        WtPrettyPrinter printer = new WtPrettyPrinter(writer);
        printer.visit(table); 
    }
    
    protected WtTableCell translateCell(CellData cell) {
        WtNode node = f.text(" ");
        if (cell != null && cell.value != null && !"".equals(cell.value)) {
            node = text(" " + cell.value.toString() + " ");
        }
        return f.td(f.emptyAttrs(), f.body(f.list(node)));
    }
    
    protected WtNode text(String text) {
        List<WtNode> nodes = new ArrayList<>();
        Matcher matcher = escapedCharacters.matcher(text);
        int endPosition = 0;
        while (matcher.find(endPosition)) {
            int newStart = matcher.start();
            nodes.add(f.text(StringEscapeUtils.escapeHtml4(text.substring(endPosition, newStart))));
            nodes.add(f.tagExt("nowiki", f.emptyAttrs(), f.tagExtBody(text.substring(newStart, matcher.end()))));
            endPosition = matcher.end();
        }
        nodes.add(f.text(StringEscapeUtils.escapeHtml4(text.substring(endPosition, text.length()))));
        return nodes.size() == 1 ? nodes.get(0) : f.list(nodes);
    }

}
