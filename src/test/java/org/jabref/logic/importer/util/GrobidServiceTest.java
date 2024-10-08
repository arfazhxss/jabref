package org.jabref.logic.importer.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.ParseException;
import org.jabref.logic.importer.fetcher.GrobidPreferences;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.testutils.category.FetcherTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FetcherTest
class GrobidServiceTest {

    private static GrobidService grobidService;
    private static ImportFormatPreferences importFormatPreferences;

    @BeforeAll
    static void setup() {
        importFormatPreferences = mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS);
        when(importFormatPreferences.bibEntryPreferences().getKeywordSeparator()).thenReturn(',');

        GrobidPreferences grobidPreferences = new GrobidPreferences(
                true,
                false,
                "http://grobid.jabref.org:8070");
        grobidService = new GrobidService(grobidPreferences);
    }

    @Test
    void processValidCitationTest() throws IOException, ParseException {
        BibEntry exampleBibEntry = new BibEntry(StandardEntryType.Article)
                .withCitationKey("-1")
                .withField(StandardField.AUTHOR, "Derwing, Tracey and Rossiter, Marian and Munro, Murray")
                .withField(StandardField.TITLE, "Teaching Native Speakers to Listen to Foreign-accented Speech")
                .withField(StandardField.JOURNAL, "Journal of Multilingual and Multicultural Development")
                .withField(StandardField.PUBLISHER, "Informa UK Limited")
                .withField(StandardField.DOI, "10.1080/01434630208666468")
                .withField(StandardField.DATE, "2002-09")
                .withField(StandardField.YEAR, "2002")
                .withField(StandardField.MONTH, "9")
                .withField(StandardField.PAGES, "245-259")
                .withField(StandardField.VOLUME, "23")
                .withField(StandardField.NUMBER, "4");
        Optional<BibEntry> response = grobidService.processCitation("Derwing, T. M., Rossiter, M. J., & Munro, " +
                "M. J. (2002). Teaching native speakers to listen to foreign-accented speech. " +
                "Journal of Multilingual and Multicultural Development, 23(4), 245-259.", importFormatPreferences, GrobidService.ConsolidateCitations.WITH_METADATA);
        assertTrue(response.isPresent());
        assertEquals(exampleBibEntry, response.get());
    }

    @Test
    void processEmptyStringTest() throws IOException, ParseException {
        Optional<BibEntry> response = grobidService.processCitation(" ", importFormatPreferences, GrobidService.ConsolidateCitations.WITH_METADATA);
        assertNotNull(response);
        assertEquals(Optional.empty(), response);
    }

    @Test
    void processInvalidCitationTest() {
        assertThrows(IOException.class, () -> grobidService.processCitation(
                "Iiiiiiiiiiiiiiiiiiiiiiii",
                importFormatPreferences,
                GrobidService.ConsolidateCitations.WITH_METADATA));
    }

    @Test
    void failsWhenGrobidDisabled() {
        GrobidPreferences importSettingsWithGrobidDisabled = new GrobidPreferences(
                false,
                false,
                "http://grobid.jabref.org:8070");
        assertThrows(UnsupportedOperationException.class, () -> new GrobidService(importSettingsWithGrobidDisabled));
    }

    @Test
    void processPdfTest() throws IOException, ParseException, URISyntaxException {
        Path file = Path.of(GrobidServiceTest.class.getResource("LNCS-minimal.pdf").toURI());
        List<BibEntry> response = grobidService.processPDF(file, importFormatPreferences);
        assertEquals(1, response.size());
        BibEntry be0 = response.getFirst();
        assertEquals(Optional.of("Lastname, Firstname"), be0.getField(StandardField.AUTHOR));
        // assertEquals(Optional.of("Paper Title"), be0.getField(StandardField.TITLE));
        // assertEquals(Optional.of("2014-10-05"), be0.getField(StandardField.DATE));
    }

    @Test
    void extractsReferencesFromPdf() throws IOException, ParseException, URISyntaxException {
        BibEntry ref1 = new BibEntry(StandardEntryType.Article)
                .withField(StandardField.AUTHOR, "Kopp, O")
                .withField(StandardField.ADDRESS, "Berlin; Heidelberg")
                .withField(StandardField.DATE, "2013")
                .withField(StandardField.JOURNAL, "All links were last followed on October")
                .withField(StandardField.PAGES, "700--704")
                .withField(StandardField.PUBLISHER, "Springer")
                .withField(StandardField.TITLE, "Winery -A Modeling Tool for TOSCA-based Cloud Applications")
                .withField(StandardField.VOLUME, "8274")
                .withField(StandardField.YEAR, "2013");

        Path file = Path.of(Objects.requireNonNull(GrobidServiceTest.class.getResource("LNCS-minimal.pdf")).toURI());
        List<BibEntry> extractedReferences = grobidService.processReferences(file, importFormatPreferences);
        assertEquals(List.of(ref1), extractedReferences);
    }
}
