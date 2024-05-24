package eu.delpeuch.antonin.refine.wikitable;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Properties;

import org.testng.annotations.Test;

import com.google.refine.RefineTest;
import com.google.refine.browsing.Engine;
import com.google.refine.model.Project;

public class WikitextExporterTests extends RefineTest {

    WikitextExporter SUT = new WikitextExporter();
    
    @Test
    public void testSimpleProject() throws IOException {
        Project project = createProject(new String[] { "foo", "bar" },
                new Serializable[][] {
            { "a", "b" },
            { null, 1 },
            { "", 2.4 },
            { "date", OffsetDateTime.parse("2027-07-09T07:08:45Z") },
            { "bool", true }
        });
        
        assertEquals(export(project), "\n"
                + "{|\n"
                + " !foo\n"
                + " !bar\n"
                + " |-\n"
                + " | a \n"
                + " | b \n"
                + " |-\n"
                + " | \n"
                + " | 1 \n"
                + " |-\n"
                + " | \n"
                + " | 2.4 \n"
                + " |-\n"
                + " | date \n"
                + " | 2027-07-09T07:08:45Z \n"
                + " |-\n"
                + " | bool \n"
                + " | true \n"
                + " |}");
    }
    
    @Test
    public void testPipeEscaping() throws IOException {
        Project project = createProject(new String[] { "foo", "bar" },
                new Serializable[][] {
            { "a", "b | c" }
        });
        
        assertEquals(export(project), "\n"
                + "{|\n"
                + " !foo\n"
                + " !bar\n"
                + " |-\n"
                + " | a \n"
                + " | b <nowiki>|</nowiki> c \n"
                + " |}");
    }
    
    @Test
    public void testMarkupEscaping() throws IOException {
        Project project = createProject(new String[] { "foo", "bar" },
                new Serializable[][] {
            { "a", "b <nowiki>" }
        });
        
        assertEquals(export(project), "\n"
                + "{|\n"
                + " !foo\n"
                + " !bar\n"
                + " |-\n"
                + " | a \n"
                + " | b &lt;nowiki&gt; \n"
                + " |}");
    }
    
    private String export(Project project) throws IOException {
        Engine engine = new Engine(project);
        StringWriter writer = new StringWriter();
        SUT.export(project, new Properties(), engine, writer);
        return writer.toString();
    }
}
