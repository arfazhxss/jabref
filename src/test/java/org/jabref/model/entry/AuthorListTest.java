package org.jabref.model.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AuthorListTest {
  /*
  Examples are similar to page 4 in
  [BibTeXing by Oren Patashnik](https://ctan.org/tex-archive/biblio/bibtex/contrib/doc/)
  */
  private static final Author MUHAMMAD_ALKHWARIZMI = new Author(
    "Mu{\\d{h}}ammad",
    "M.",
    null,
    "al-Khw{\\={a}}rizm{\\={i}}",
    null
  );
  private static final Author CORRADO_BOHM = new Author(
    "Corrado",
    "C.",
    null,
    "B{\\\"o}hm",
    null
  );
  private static final Author KURT_GODEL = new Author(
    "Kurt",
    "K.",
    null,
    "G{\\\"{o}}del",
    null
  );
  private static final Author BANU_MOSA = new Author(
    null,
    null,
    null,
    "{The Ban\\={u} M\\={u}s\\={a} brothers}",
    null
  );
  private static final AuthorList EMPTY_AUTHOR = AuthorList.of(
    Collections.emptyList()
  );
  private static final AuthorList ONE_AUTHOR_WITH_LATEX = AuthorList.of(
    MUHAMMAD_ALKHWARIZMI
  );
  private static final AuthorList TWO_AUTHORS_WITH_LATEX = AuthorList.of(
    MUHAMMAD_ALKHWARIZMI,
    CORRADO_BOHM
  );
  private static final AuthorList THREE_AUTHORS_WITH_LATEX = AuthorList.of(
    MUHAMMAD_ALKHWARIZMI,
    CORRADO_BOHM,
    KURT_GODEL
  );
  private static final AuthorList ONE_INSTITUTION_WITH_LATEX = AuthorList.of(
    BANU_MOSA
  );
  private static final AuthorList ONE_INSTITUTION_WITH_STARTING_PARANTHESIS =
    AuthorList.of(
      new Author(null, null, null, "{{\\L{}}ukasz Micha\\l{}}", null)
    );
  private static final AuthorList TWO_INSTITUTIONS_WITH_LATEX = AuthorList.of(
    BANU_MOSA,
    BANU_MOSA
  );
  private static final AuthorList MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX =
    AuthorList.of(BANU_MOSA, CORRADO_BOHM);

  AuthorList authorList = AuthorList.parse("Doe, John and Smith, Jane");

  public static int size(String bibtex) {
    return AuthorList.parse(bibtex).getNumberOfAuthors();
  }

  @Test
  public void testFixAuthorNatbib() {
    assertEquals("", AuthorList.fixAuthorNatbib(""));
    assertEquals("Smith", AuthorList.fixAuthorNatbib("John Smith"));
    assertEquals(
      "Smith and Black Brown",
      AuthorList.fixAuthorNatbib("John Smith and Black Brown, Peter")
    );
    assertEquals(
      "von Neumann et al.",
      AuthorList.fixAuthorNatbib(
        "John von Neumann and John Smith and Black Brown, Peter"
      )
    );
  }

  @CsvSource(
    value = {
      "''; '';",
      "'Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}}'; 'al-Khwārizmī';",
      "'Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm'; 'al-Khwārizmī and Böhm';",
      "'Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm and Kurt Godel'; 'al-Khwārizmī et al.';",
      "'{The Ban\\={u} M\\={u}s\\={a} brothers}'; 'The Banū Mūsā brothers';",
      "'{The Ban\\={u} M\\={u}s\\={a} brothers} and {The Ban\\={u} M\\={u}s\\={a} brothers}'; 'The Banū Mūsā brothers and The Banū Mūsā brothers';",
      "'{The Ban\\={u} M\\={u}s\\={a} brothers} and Corrado B{\\\"o}hm'; 'The Banū Mūsā brothers and Böhm';",
      "'{{\\L{}}ukasz Micha\\l{}}'; 'Łukasz Michał';",
    },
    delimiter = ';'
  )
  @ParameterizedTest
  public void getAsNatbibLatexFree(AuthorList input, String expected) {
    assertEquals(expected, input.latexFree().getAsNatbib());
  }

  @Test
  public void parseCachesOneAuthor() {
    AuthorList authorList = AuthorList.parse("John Smith");
    assertSame(authorList, AuthorList.parse("John Smith"));
    assertNotSame(authorList, AuthorList.parse("Smith"));
  }

  @Test
  public void parseCachesOneLatexFreeAuthor() {
    AuthorList authorList = AuthorList.parse("John Smith").latexFree();
    assertSame(authorList, AuthorList.parse("John Smith").latexFree());
    assertNotSame(authorList, AuthorList.parse("Smith").latexFree());
  }

  @Test
  public void testFixAuthorFirstNameFirstCommas() {
    assertEquals("", AuthorList.fixAuthorFirstNameFirstCommas("", true, false));
    assertEquals(
      "",
      AuthorList.fixAuthorFirstNameFirstCommas("", false, false)
    );
    assertEquals(
      "John Smith",
      AuthorList.fixAuthorFirstNameFirstCommas("John Smith", false, false)
    );
    assertEquals(
      "J. Smith",
      AuthorList.fixAuthorFirstNameFirstCommas("John Smith", true, false)
    );

    assertEquals(
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        true,
        false
      ),
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        true,
        false
      )
    );
    assertEquals(
      "John Smith and Peter Black Brown",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John Smith and Black Brown, Peter",
        false,
        false
      )
    );
    assertEquals(
      "J. Smith and P. Black Brown",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John Smith and Black Brown, Peter",
        true,
        false
      )
    );

    assertEquals(
      "John von Neumann, John Smith and Peter Black Brown",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        false,
        false
      )
    );
    assertEquals(
      "J. von Neumann, J. Smith and P. Black Brown",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        true,
        false
      )
    );
    assertEquals(
      "J. P. von Neumann",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John Peter von Neumann",
        true,
        false
      )
    );

    assertEquals("", AuthorList.fixAuthorFirstNameFirstCommas("", true, true));
    assertEquals("", AuthorList.fixAuthorFirstNameFirstCommas("", false, true));
    assertEquals(
      "John Smith",
      AuthorList.fixAuthorFirstNameFirstCommas("John Smith", false, true)
    );
    assertEquals(
      "J. Smith",
      AuthorList.fixAuthorFirstNameFirstCommas("John Smith", true, true)
    );

    assertEquals(
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        true,
        true
      ),
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        true,
        true
      )
    );
    assertEquals(
      "John Smith and Peter Black Brown",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John Smith and Black Brown, Peter",
        false,
        true
      )
    );
    assertEquals(
      "J. Smith and P. Black Brown",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John Smith and Black Brown, Peter",
        true,
        true
      )
    );

    assertEquals(
      "John von Neumann, John Smith, and Peter Black Brown",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        false,
        true
      )
    );
    assertEquals(
      "J. von Neumann, J. Smith, and P. Black Brown",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        true,
        true
      )
    );
    assertEquals(
      "J. P. von Neumann",
      AuthorList.fixAuthorFirstNameFirstCommas(
        "John Peter von Neumann",
        true,
        true
      )
    );
  }

  @CsvSource(
    {
      "'', true, false, ''",
      "'M. al-Khwārizmī', true, false, 'Mudhammad al-Khwārizmī'",
      "'M. al-Khwārizmī and C. Böhm', true, false, 'Mudhammad al-Khwārizmī and Corrado Böhm'",
      "'M. al-Khwārizmī and C. Böhm', true, true, 'Mudhammad al-Khwārizmī and Corrado Böhm'",
      "'T. B. M. brothers', true, false, 'The Banū Mūsā brothers'",
      "'T. B. M. brothers and T. B. M. brothers', true, false, 'The Banū Mūsā brothers and The Banū Mūsā brothers'",
      "'T. B. M. brothers and C. Böhm', true, false, 'The Banū Mūsā brothers and Corrado Böhm'",
      "'Ł. Michał', true, false, 'Łukasz Michał'",
      "'', false, false, ''",
      "'Muḥammad al-Khwārizmī', false, false, 'Muḥammad al-Khwārizmī'",
      "'Muḥammad al-Khwārizmī and Corrado Böhm', false, false, 'Muḥammad al-Khwārizmī and Corrado Böhm'",
      "'The Banū Mūsā brothers', false, false, 'The Banū Mūsā brothers'",
      "'The Banū Mūsā brothers and The Banū Mūsā brothers', false, false, 'The Banū Mūsā brothers and The Banū Mūsā brothers'",
      "'Łukasz Michał', false, false, 'Łukasz Michał'",
    }
  )
  @ParameterizedTest
  public void testGetAsFirstLastNamesLatexFree(
    String expected,
    boolean abbreviate,
    boolean oxfordComma,
    AuthorList input
  ) {
    assertEquals(
      expected,
      input.latexFree().getAsFirstLastNames(abbreviate, oxfordComma)
    );
  }

  @Test
  public void testFixAuthorFirstNameFirst() {
    assertEquals(
      "John Smith",
      AuthorList.fixAuthorFirstNameFirst("John Smith")
    );
    assertEquals(
      "John Smith and Peter Black Brown",
      AuthorList.fixAuthorFirstNameFirst("John Smith and Black Brown, Peter")
    );
    assertEquals(
      "John von Neumann and John Smith and Peter Black Brown",
      AuthorList.fixAuthorFirstNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter"
      )
    );
    assertEquals(
      "First von Last, Jr. III",
      AuthorList.fixAuthorFirstNameFirst("von Last, Jr. III, First")
    );

    assertEquals(
      AuthorList.fixAuthorFirstNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter"
      ),
      AuthorList.fixAuthorFirstNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter"
      )
    );
  }

  @Test
  public void testFixAuthorLastNameFirstCommasNoComma() {
    assertEquals("", AuthorList.fixAuthorLastNameFirstCommas("", true, false));
    assertEquals("", AuthorList.fixAuthorLastNameFirstCommas("", false, false));
    assertEquals(
      "Smith, John",
      AuthorList.fixAuthorLastNameFirstCommas("John Smith", false, false)
    );
    assertEquals(
      "Smith, J.",
      AuthorList.fixAuthorLastNameFirstCommas("John Smith", true, false)
    );
    String a = AuthorList.fixAuthorLastNameFirstCommas(
      "John von Neumann and John Smith and Black Brown, Peter",
      true,
      false
    );
    String b = AuthorList.fixAuthorLastNameFirstCommas(
      "John von Neumann and John Smith and Black Brown, Peter",
      true,
      false
    );

    assertEquals(a, b);
    assertEquals(
      "Smith, John and Black Brown, Peter",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John Smith and Black Brown, Peter",
        false,
        false
      )
    );
    assertEquals(
      "Smith, J. and Black Brown, P.",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John Smith and Black Brown, Peter",
        true,
        false
      )
    );
    assertEquals(
      "von Neumann, John, Smith, John and Black Brown, Peter",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        false,
        false
      )
    );
    assertEquals(
      "von Neumann, J., Smith, J. and Black Brown, P.",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        true,
        false
      )
    );
    assertEquals(
      "von Neumann, J. P.",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John Peter von Neumann",
        true,
        false
      )
    );
  }

  @Test
  public void testFixAuthorLastNameFirstCommasOxfordComma() {
    assertEquals("", AuthorList.fixAuthorLastNameFirstCommas("", true, true));
    assertEquals("", AuthorList.fixAuthorLastNameFirstCommas("", false, true));
    assertEquals(
      "Smith, John",
      AuthorList.fixAuthorLastNameFirstCommas("John Smith", false, true)
    );
    assertEquals(
      "Smith, J.",
      AuthorList.fixAuthorLastNameFirstCommas("John Smith", true, true)
    );
    String a = AuthorList.fixAuthorLastNameFirstCommas(
      "John von Neumann and John Smith and Black Brown, Peter",
      true,
      true
    );
    String b = AuthorList.fixAuthorLastNameFirstCommas(
      "John von Neumann and John Smith and Black Brown, Peter",
      true,
      true
    );

    assertEquals(a, b);
    assertEquals(
      "Smith, John and Black Brown, Peter",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John Smith and Black Brown, Peter",
        false,
        true
      )
    );
    assertEquals(
      "Smith, J. and Black Brown, P.",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John Smith and Black Brown, Peter",
        true,
        true
      )
    );
    assertEquals(
      "von Neumann, John, Smith, John, and Black Brown, Peter",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        false,
        true
      )
    );
    assertEquals(
      "von Neumann, J., Smith, J., and Black Brown, P.",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John von Neumann and John Smith and Black Brown, Peter",
        true,
        true
      )
    );
    assertEquals(
      "von Neumann, J. P.",
      AuthorList.fixAuthorLastNameFirstCommas(
        "John Peter von Neumann",
        true,
        true
      )
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeEmptyAuthorStringForEmptyInputAbbr() {
    assertEquals("", EMPTY_AUTHOR.latexFree().getAsLastFirstNames(true, false));
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeOneAuthorNameFromLatexAbbr() {
    assertEquals(
      "al-Khwārizmī, M.",
      ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastFirstNames(true, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeTwoAuthorNamesFromLatexAbbr() {
    assertEquals(
      "al-Khwārizmī, M. and Böhm, C.",
      TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(true, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeThreeUnicodeAuthorsFromLatexAbbr() {
    assertEquals(
      "al-Khwārizmī, M., Böhm, C. and Gödel, K.",
      THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(true, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeOneInsitutionNameFromLatexAbbr() {
    assertEquals(
      "The Banū Mūsā brothers",
      ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(true, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeTwoInsitutionNameFromLatexAbbr() {
    assertEquals(
      "The Banū Mūsā brothers and The Banū Mūsā brothers",
      TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastFirstNames(true, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeMixedAuthorsFromLatexAbbr() {
    assertEquals(
      "The Banū Mūsā brothers and Böhm, C.",
      MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree()
        .getAsLastFirstNames(true, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeOneInstitutionWithParanthesisAtStartAbbr() {
    assertEquals(
      "Łukasz Michał",
      ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree()
        .getAsLastFirstNames(true, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeEmptyAuthorStringForEmptyInput() {
    assertEquals(
      "",
      EMPTY_AUTHOR.latexFree().getAsLastFirstNames(false, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeOneAuthorNameFromLatex() {
    assertEquals(
      "al-Khwārizmī, Muḥammad",
      ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastFirstNames(false, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeTwoAuthorNamesFromLatex() {
    assertEquals(
      "al-Khwārizmī, Muḥammad and Böhm, Corrado",
      TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(false, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeThreeUnicodeAuthorsFromLatex() {
    assertEquals(
      "al-Khwārizmī, Muḥammad, Böhm, Corrado and Gödel, Kurt",
      THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(false, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeOneInsitutionNameFromLatex() {
    assertEquals(
      "The Banū Mūsā brothers",
      ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(false, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeTwoInsitutionNameFromLatex() {
    assertEquals(
      "The Banū Mūsā brothers and The Banū Mūsā brothers",
      TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastFirstNames(false, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeMixedAuthorsFromLatex() {
    assertEquals(
      "The Banū Mūsā brothers and Böhm, Corrado",
      MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree()
        .getAsLastFirstNames(false, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeOneInstitutionWithParanthesisAtStart() {
    assertEquals(
      "Łukasz Michał",
      ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree()
        .getAsLastFirstNames(false, false)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeEmptyAuthorStringForEmptyInputAbbrOxfordComma() {
    assertEquals("", EMPTY_AUTHOR.latexFree().getAsLastFirstNames(true, true));
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeOneAuthorNameFromLatexAbbrOxfordComma() {
    assertEquals(
      "al-Khwārizmī, M.",
      ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastFirstNames(true, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeTwoAuthorNamesFromLatexAbbrOxfordComma() {
    assertEquals(
      "al-Khwārizmī, M. and Böhm, C.",
      TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(true, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeThreeUnicodeAuthorsFromLatexAbbrOxfordComma() {
    assertEquals(
      "al-Khwārizmī, M., Böhm, C., and Gödel, K.",
      THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(true, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeOneInsitutionNameFromLatexAbbrOxfordComma() {
    assertEquals(
      "The Banū Mūsā brothers",
      ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(true, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeTwoInsitutionNameFromLatexAbbrOxfordComma() {
    assertEquals(
      "The Banū Mūsā brothers and The Banū Mūsā brothers",
      TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastFirstNames(true, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeMixedAuthorsFromLatexAbbrOxfordComma() {
    assertEquals(
      "The Banū Mūsā brothers and Böhm, C.",
      MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree()
        .getAsLastFirstNames(true, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeOneInstitutionWithParanthesisAtStartAbbrOxfordComma() {
    assertEquals(
      "Łukasz Michał",
      ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree()
        .getAsLastFirstNames(true, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeEmptyAuthorStringForEmptyInputOxfordComma() {
    assertEquals("", EMPTY_AUTHOR.latexFree().getAsLastFirstNames(false, true));
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeOneAuthorNameFromLatexOxfordComma() {
    assertEquals(
      "al-Khwārizmī, Muḥammad",
      ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastFirstNames(false, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeTwoAuthorNamesFromLatexOxfordComma() {
    assertEquals(
      "al-Khwārizmī, Muḥammad and Böhm, Corrado",
      TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(false, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeThreeUnicodeAuthorsFromLatexOxfordComma() {
    assertEquals(
      "al-Khwārizmī, Muḥammad, Böhm, Corrado, and Gödel, Kurt",
      THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(false, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeOneInsitutionNameFromLatexOxfordComma() {
    assertEquals(
      "The Banū Mūsā brothers",
      ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(false, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeTwoInsitutionNameFromLatexOxfordComma() {
    assertEquals(
      "The Banū Mūsā brothers and The Banū Mūsā brothers",
      TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastFirstNames(false, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeUnicodeMixedAuthorsFromLatexOxfordComma() {
    assertEquals(
      "The Banū Mūsā brothers and Böhm, Corrado",
      MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree()
        .getAsLastFirstNames(false, true)
    );
  }

  @Test
  public void getAsLastFirstNamesLatexFreeOneInstitutionWithParanthesisAtStartOxfordComma() {
    assertEquals(
      "Łukasz Michał",
      ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree()
        .getAsLastFirstNames(false, true)
    );
  }

  @Test
  public void testFixAuthorLastNameFirst() {
    assertEquals(
      "Smith, John",
      AuthorList.fixAuthorLastNameFirst("John Smith")
    );
    assertEquals(
      "Smith, John and Black Brown, Peter",
      AuthorList.fixAuthorLastNameFirst("John Smith and Black Brown, Peter")
    );
    assertEquals(
      "von Neumann, John and Smith, John and Black Brown, Peter",
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter"
      )
    );
    assertEquals(
      "von Last, Jr, First",
      AuthorList.fixAuthorLastNameFirst("von Last, Jr ,First")
    );
    assertEquals(
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter"
      ),
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter"
      )
    );

    assertEquals(
      "Smith, John",
      AuthorList.fixAuthorLastNameFirst("John Smith", false)
    );
    assertEquals(
      "Smith, John and Black Brown, Peter",
      AuthorList.fixAuthorLastNameFirst(
        "John Smith and Black Brown, Peter",
        false
      )
    );
    assertEquals(
      "von Neumann, John and Smith, John and Black Brown, Peter",
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter",
        false
      )
    );
    assertEquals(
      "von Last, Jr, First",
      AuthorList.fixAuthorLastNameFirst("von Last, Jr ,First", false)
    );
    assertEquals(
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter",
        false
      ),
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter",
        false
      )
    );

    assertEquals(
      "Smith, J.",
      AuthorList.fixAuthorLastNameFirst("John Smith", true)
    );
    assertEquals(
      "Smith, J. and Black Brown, P.",
      AuthorList.fixAuthorLastNameFirst(
        "John Smith and Black Brown, Peter",
        true
      )
    );
    assertEquals(
      "von Neumann, J. and Smith, J. and Black Brown, P.",
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter",
        true
      )
    );
    assertEquals(
      "von Last, Jr, F.",
      AuthorList.fixAuthorLastNameFirst("von Last, Jr ,First", true)
    );
    assertEquals(
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter",
        true
      ),
      AuthorList.fixAuthorLastNameFirst(
        "John von Neumann and John Smith and Black Brown, Peter",
        true
      )
    );
  }

  @CsvSource(
    value = {
      "''; false; ''",
      "John Smith; false; Smith",
      "''; true; ''",
      "John Smith; true; Smith",
      "Smith, Jr, John; false; Smith",
      "John von Neumann and John Smith and Black Brown, Peter; false; von Neumann, Smith and Black Brown",
      "Smith, Jr, John; true; Smith",
      "John von Neumann and John Smith and Black Brown, Peter; true; von Neumann, Smith, and Black Brown",
    },
    delimiter = ';'
  )
  @ParameterizedTest
  public void testFixAuthorLastNameOnlyCommas(
    String input,
    boolean oxfordComma,
    String expectedOutput
  ) {
    assertEquals(
      expectedOutput,
      AuthorList.fixAuthorLastNameOnlyCommas(input, oxfordComma)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeUnicodeOneAuthorNameFromLatex() {
    assertEquals(
      "al-Khwārizmī",
      ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastNames(false)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeUnicodeTwoAuthorNamesFromLatex() {
    assertEquals(
      "al-Khwārizmī and Böhm",
      TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastNames(false)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeUnicodeTwoAuthorNamesFromLatexUsingOxfordComma() {
    assertEquals(
      "al-Khwārizmī and Böhm",
      TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastNames(true)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeUnicodeThreeAuthorsFromLatex() {
    assertEquals(
      "al-Khwārizmī, Böhm and Gödel",
      THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastNames(false)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeUnicodeThreeAuthorsFromLatexUsingOxfordComma() {
    assertEquals(
      "al-Khwārizmī, Böhm, and Gödel",
      THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastNames(true)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeUnicodeOneInsitutionNameFromLatex() {
    assertEquals(
      "The Banū Mūsā brothers",
      ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastNames(false)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeUnicodeTwoInsitutionNameFromLatex() {
    assertEquals(
      "The Banū Mūsā brothers and The Banū Mūsā brothers",
      TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastNames(false)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeUnicodeMixedAuthorsFromLatex() {
    assertEquals(
      "The Banū Mūsā brothers and Böhm",
      MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsLastNames(false)
    );
  }

  @Test
  public void getAsLastNamesLatexFreeOneInstitutionWithParanthesisAtStart() {
    assertEquals(
      "Łukasz Michał",
      ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree()
        .getAsLastNames(false)
    );
  }

  @Test
  public void testFixAuthorForAlphabetization() {
    assertEquals(
      "Smith, J.",
      AuthorList.fixAuthorForAlphabetization("John Smith")
    );
    assertEquals(
      "Neumann, J.",
      AuthorList.fixAuthorForAlphabetization("John von Neumann")
    );
    assertEquals(
      "Neumann, J.",
      AuthorList.fixAuthorForAlphabetization("J. von Neumann")
    );
    assertEquals(
      "Neumann, J. and Smith, J. and Black Brown, Jr., P.",
      AuthorList.fixAuthorForAlphabetization(
        "John von Neumann and John Smith and de Black Brown, Jr., Peter"
      )
    );
  }

  @Test
  public void testSize() {
    assertEquals(0, AuthorListTest.size(""));
    assertEquals(1, AuthorListTest.size("Bar"));
    assertEquals(1, AuthorListTest.size("Foo Bar"));
    assertEquals(1, AuthorListTest.size("Foo von Bar"));
    assertEquals(1, AuthorListTest.size("von Bar, Foo"));
    assertEquals(1, AuthorListTest.size("Bar, Foo"));
    assertEquals(1, AuthorListTest.size("Bar, Jr., Foo"));
    assertEquals(1, AuthorListTest.size("Bar, Foo"));
    assertEquals(2, AuthorListTest.size("John Neumann and Foo Bar"));
    assertEquals(2, AuthorListTest.size("John von Neumann and Bar, Jr, Foo"));
    assertEquals(
      3,
      AuthorListTest.size(
        "John von Neumann and John Smith and Black Brown, Peter"
      )
    );

    StringBuilder s = new StringBuilder("John von Neumann");
    for (int i = 0; i < 25; i++) {
      assertEquals(i + 1, AuthorListTest.size(s.toString()));
      s.append(" and Albert Einstein");
    }
  }

  @Test
  public void testIsEmpty() {
    assertTrue(AuthorList.parse("").isEmpty());
    assertFalse(AuthorList.parse("Bar").isEmpty());
  }

  @Test
  public void testGetEmptyAuthor() {
    assertThrows(Exception.class, () -> AuthorList.parse("").getAuthor(0));
  }

  @Test
  public void GetAuthor() {
    testAuthor(
      0,
      "John Smith and von Neumann, Jr, John",
      "John",
      "J.",
      "John Smith",
      "J. Smith",
      Optional.empty(),
      "Smith",
      "Smith, John",
      "Smith, J.",
      "Smith",
      "Smith, J.",
      Optional.empty()
    );
    testAuthor(
      0,
      "Peter Black Brown",
      "Peter Black",
      "P. B.",
      "Peter Black Brown",
      "P. B. Brown",
      Optional.empty(),
      "Brown",
      "Brown, Peter Black",
      "Brown, P. B.",
      "Brown",
      "Brown, P. B.",
      Optional.empty()
    );
    testAuthor(
      1,
      "John Smith and von Neumann, Jr, John",
      "John",
      "J.",
      "John von Neumann, Jr",
      "J. von Neumann, Jr",
      Optional.of("Jr"),
      "Neumann",
      "von Neumann, Jr, John",
      "von Neumann, Jr, J.",
      "von Neumann",
      "Neumann, Jr, J.",
      Optional.of("von")
    );
  }

  private void testAuthor(
    int authorIndex,
    String inputString,
    String expectedFirst,
    String expectedFirstAbbr,
    String expectedFirstLast,
    String expectedFirstLastAbbr,
    Optional<String> expectedJr,
    String expectedLast,
    String expectedLastFirst,
    String expectedLastFirstAbbr,
    String expectedLastOnly,
    String expectedNameForAlphabetization,
    Optional<String> expectedVon
  ) {
    Author author = AuthorList.parse(inputString).getAuthor(authorIndex);
    assertEquals(Optional.of(expectedFirst), author.getGivenName());
    assertEquals(
      Optional.of(expectedFirstAbbr),
      author.getGivenNameAbbreviated()
    );
    assertEquals(expectedFirstLast, author.getGivenFamily(false));
    assertEquals(expectedFirstLastAbbr, author.getGivenFamily(true));
    assertEquals(expectedJr, author.getNameSuffix());
    assertEquals(Optional.of(expectedLast), author.getFamilyName());
    assertEquals(expectedLastFirst, author.getFamilyGiven(false));
    assertEquals(expectedLastFirstAbbr, author.getFamilyGiven(true));
    assertEquals(expectedLastOnly, author.getNamePrefixAndFamilyName());
    assertEquals(
      expectedNameForAlphabetization,
      author.getNameForAlphabetization()
    );
    assertEquals(expectedVon, author.getNamePrefix());
  }

  @Test
  public void testCompanyAuthor() {
    Author author = AuthorList.parse("{JabRef Developers}").getAuthor(0);
    Author expected = new Author(null, null, null, "{JabRef Developers}", null);
    assertEquals(expected, author);
  }

  @Test
  public void testCompanyAuthorAndPerson() {
    Author company = new Author(null, null, null, "{JabRef Developers}", null);
    Author person = new Author("Stefan", "S.", null, "Kolb", null);
    assertEquals(
      Arrays.asList(company, person),
      AuthorList.parse("{JabRef Developers} and Stefan Kolb").getAuthors()
    );
  }

  @Test
  public void testCompanyAuthorWithLowerCaseWord() {
    Author author = AuthorList.parse("{JabRef Developers on Fire}").getAuthor(
      0
    );
    Author expected = new Author(
      null,
      null,
      null,
      "{JabRef Developers on Fire}",
      null
    );
    assertEquals(expected, author);
  }

  @Test
  public void testAbbreviationWithRelax() {
    Author author = AuthorList.parse("{\\relax Ch}ristoph Cholera").getAuthor(
      0
    );
    Author expected = new Author(
      "{\\relax Ch}ristoph",
      "{\\relax Ch}.",
      null,
      "Cholera",
      null
    );
    assertEquals(expected, author);
  }

  @Test
  public void testGetAuthorsNatbib() {
    assertEquals("", AuthorList.parse("").getAsNatbib());
    assertEquals("Smith", AuthorList.parse("John Smith").getAsNatbib());
    assertEquals(
      "Smith and Black Brown",
      AuthorList.parse("John Smith and Black Brown, Peter").getAsNatbib()
    );
    assertEquals(
      "von Neumann et al.",
      AuthorList.parse(
        "John von Neumann and John Smith and Black Brown, Peter"
      ).getAsNatbib()
    );

    assertEquals(
      "Last-Name et al.",
      AuthorList.parse(
        "First Second Last-Name" + " and John Smith and Black Brown, Peter"
      ).getAsNatbib()
    );

    AuthorList al = AuthorList.parse(
      "John von Neumann and John Smith and Black Brown, Peter"
    );
    assertEquals(al.getAsNatbib(), al.getAsNatbib());
  }

  @Test
  public void testGetAuthorsLastOnly() {
    assertEquals("", AuthorList.parse("").getAsLastNames(false));
    assertEquals("Smith", AuthorList.parse("John Smith").getAsLastNames(false));
    assertEquals(
      "Smith",
      AuthorList.parse("Smith, Jr, John").getAsLastNames(false)
    );
    assertEquals(
      "von Neumann, Smith and Black Brown",
      AuthorList.parse(
        "John von Neumann and John Smith and Black Brown, Peter"
      ).getAsLastNames(false)
    );

    assertEquals("", AuthorList.parse("").getAsLastNames(true));
    assertEquals("Smith", AuthorList.parse("John Smith").getAsLastNames(true));
    assertEquals(
      "Smith",
      AuthorList.parse("Smith, Jr, John").getAsLastNames(true)
    );
    assertEquals(
      "von Neumann, Smith, and Black Brown",
      AuthorList.parse(
        "John von Neumann and John Smith and Black Brown, Peter"
      ).getAsLastNames(true)
    );
    assertEquals(
      "von Neumann and Smith",
      AuthorList.parse("John von Neumann and John Smith").getAsLastNames(false)
    );
  }

  @Test
  public void testGetAuthorsLastFirstNoComma() {
    AuthorList al;

    al = AuthorList.parse("");
    assertEquals("", al.getAsLastFirstNames(true, false));
    assertEquals("", al.getAsLastFirstNames(false, false));

    al = AuthorList.parse("John Smith");
    assertEquals("Smith, John", al.getAsLastFirstNames(false, false));
    assertEquals("Smith, J.", al.getAsLastFirstNames(true, false));

    al = AuthorList.parse("John Smith and Black Brown, Peter");
    assertEquals(
      "Smith, John and Black Brown, Peter",
      al.getAsLastFirstNames(false, false)
    );
    assertEquals(
      "Smith, J. and Black Brown, P.",
      al.getAsLastFirstNames(true, false)
    );

    al = AuthorList.parse(
      "John von Neumann and John Smith and Black Brown, Peter"
    );

    assertEquals(
      "von Neumann, John, Smith, John and Black Brown, Peter",
      al.getAsLastFirstNames(false, false)
    );
    assertEquals(
      "von Neumann, J., Smith, J. and Black Brown, P.",
      al.getAsLastFirstNames(true, false)
    );

    al = AuthorList.parse("John Peter von Neumann");
    assertEquals("von Neumann, J. P.", al.getAsLastFirstNames(true, false));
  }

  @Test
  public void testGetAuthorsLastFirstOxfordComma() {
    AuthorList al;

    al = AuthorList.parse("");
    assertEquals("", al.getAsLastFirstNames(true, true));
    assertEquals("", al.getAsLastFirstNames(false, true));

    al = AuthorList.parse("John Smith");
    assertEquals("Smith, John", al.getAsLastFirstNames(false, true));
    assertEquals("Smith, J.", al.getAsLastFirstNames(true, true));

    al = AuthorList.parse("John Smith and Black Brown, Peter");
    assertEquals(
      "Smith, John and Black Brown, Peter",
      al.getAsLastFirstNames(false, true)
    );
    assertEquals(
      "Smith, J. and Black Brown, P.",
      al.getAsLastFirstNames(true, true)
    );

    al = AuthorList.parse(
      "John von Neumann and John Smith and Black Brown, Peter"
    );
    assertEquals(
      "von Neumann, John, Smith, John, and Black Brown, Peter",
      al.getAsLastFirstNames(false, true)
    );
    assertEquals(
      "von Neumann, J., Smith, J., and Black Brown, P.",
      al.getAsLastFirstNames(true, true)
    );
    al = AuthorList.parse("John Peter von Neumann");
    assertEquals("von Neumann, J. P.", al.getAsLastFirstNames(true, true));
  }

  @Test
  public void testGetAuthorsLastFirstAnds() {
    assertEquals(
      "Smith, John",
      AuthorList.parse("John Smith").getAsLastFirstNamesWithAnd(false)
    );
    assertEquals(
      "Smith, John and Black Brown, Peter",
      AuthorList.parse(
        "John Smith and Black Brown, Peter"
      ).getAsLastFirstNamesWithAnd(false)
    );
    assertEquals(
      "von Neumann, John and Smith, John and Black Brown, Peter",
      AuthorList.parse(
        "John von Neumann and John Smith and Black Brown, Peter"
      ).getAsLastFirstNamesWithAnd(false)
    );
    assertEquals(
      "von Last, Jr, First",
      AuthorList.parse("von Last, Jr ,First").getAsLastFirstNamesWithAnd(false)
    );
    assertEquals(
      "Smith, J.",
      AuthorList.parse("John Smith").getAsLastFirstNamesWithAnd(true)
    );
    assertEquals(
      "Smith, J. and Black Brown, P.",
      AuthorList.parse(
        "John Smith and Black Brown, Peter"
      ).getAsLastFirstNamesWithAnd(true)
    );
    assertEquals(
      "von Neumann, J. and Smith, J. and Black Brown, P.",
      AuthorList.parse(
        "John von Neumann and John Smith and Black Brown, Peter"
      ).getAsLastFirstNamesWithAnd(true)
    );
    assertEquals(
      "von Last, Jr, F.",
      AuthorList.parse("von Last, Jr ,First").getAsLastFirstNamesWithAnd(true)
    );
  }

  @Test
  public void testGetAuthorsFirstFirst() {
    AuthorList al;

    al = AuthorList.parse("");
    assertEquals("", al.getAsFirstLastNames(true, false));
    assertEquals("", al.getAsFirstLastNames(false, false));
    assertEquals("", al.getAsFirstLastNames(true, true));
    assertEquals("", al.getAsFirstLastNames(false, true));

    al = AuthorList.parse("John Smith");
    assertEquals("John Smith", al.getAsFirstLastNames(false, false));
    assertEquals("J. Smith", al.getAsFirstLastNames(true, false));
    assertEquals("John Smith", al.getAsFirstLastNames(false, true));
    assertEquals("J. Smith", al.getAsFirstLastNames(true, true));

    al = AuthorList.parse("John Smith and Black Brown, Peter");
    assertEquals(
      "John Smith and Peter Black Brown",
      al.getAsFirstLastNames(false, false)
    );
    assertEquals(
      "J. Smith and P. Black Brown",
      al.getAsFirstLastNames(true, false)
    );
    assertEquals(
      "John Smith and Peter Black Brown",
      al.getAsFirstLastNames(false, true)
    );
    assertEquals(
      "J. Smith and P. Black Brown",
      al.getAsFirstLastNames(true, true)
    );

    al = AuthorList.parse(
      "John von Neumann and John Smith and Black Brown, Peter"
    );
    assertEquals(
      "John von Neumann, John Smith and Peter Black Brown",
      al.getAsFirstLastNames(false, false)
    );
    assertEquals(
      "J. von Neumann, J. Smith and P. Black Brown",
      al.getAsFirstLastNames(true, false)
    );
    assertEquals(
      "John von Neumann, John Smith, and Peter Black Brown",
      al.getAsFirstLastNames(false, true)
    );
    assertEquals(
      "J. von Neumann, J. Smith, and P. Black Brown",
      al.getAsFirstLastNames(true, true)
    );

    al = AuthorList.parse("John Peter von Neumann");
    assertEquals(
      "John Peter von Neumann",
      al.getAsFirstLastNames(false, false)
    );
    assertEquals("John Peter von Neumann", al.getAsFirstLastNames(false, true));
    assertEquals("J. P. von Neumann", al.getAsFirstLastNames(true, false));
    assertEquals("J. P. von Neumann", al.getAsFirstLastNames(true, true));
  }

  @Test
  public void testGetAuthorsFirstFirstAnds() {
    assertEquals(
      "John Smith",
      AuthorList.parse("John Smith").getAsFirstLastNamesWithAnd()
    );
    assertEquals(
      "John Smith and Peter Black Brown",
      AuthorList.parse(
        "John Smith and Black Brown, Peter"
      ).getAsFirstLastNamesWithAnd()
    );
    assertEquals(
      "John von Neumann and John Smith and Peter Black Brown",
      AuthorList.parse(
        "John von Neumann and John Smith and Black Brown, Peter"
      ).getAsFirstLastNamesWithAnd()
    );
    assertEquals(
      "First von Last, Jr. III",
      AuthorList.parse("von Last, Jr. III, First").getAsFirstLastNamesWithAnd()
    );
  }

  @Test
  public void testGetAuthorsForAlphabetization() {
    assertEquals(
      "Smith, J.",
      AuthorList.parse("John Smith").getForAlphabetization()
    );
    assertEquals(
      "Neumann, J.",
      AuthorList.parse("John von Neumann").getForAlphabetization()
    );
    assertEquals(
      "Neumann, J.",
      AuthorList.parse("J. von Neumann").getForAlphabetization()
    );
    assertEquals(
      "Neumann, J. and Smith, J. and Black Brown, Jr., P.",
      AuthorList.parse(
        "John von Neumann and John Smith and de Black Brown, Jr., Peter"
      ).getForAlphabetization()
    );
  }

  @ParameterizedTest
  @CsvSource(
    {
      "{A}bbb{c},{A}bbb{c}",
      "{Vall{\\'e}e Poussin},{Vall{\\'e}e Poussin}",
      "{Vall{\\'e}e} {Poussin},Poussin",
      "Vall{\\'e}e Poussin,Poussin",
      "Firstname {Lastname},Lastname",
      "{Firstname Lastname},{Firstname Lastname}",
    }
  )
  public void testRemoveStartAndEndBraces(String input, String expected) {
    assertEquals(expected, AuthorList.parse(input).getAsLastNames(false));
  }

  @Test
  public void createCorrectInitials() {
    assertEquals(
      Optional.of("J. G."),
      AuthorList.parse("Hornberg, Johann Gottfried")
        .getAuthor(0)
        .getGivenNameAbbreviated()
    );
  }

  @Test
  public void parseNameWithBracesAroundFirstName() throws Exception {
    Author expected = new Author("Tse-tung", "{Tse-tung}.", null, "Mao", null);
    assertEquals(AuthorList.of(expected), AuthorList.parse("{Tse-tung} Mao"));
  }

  @Test
  public void parseNameWithBracesAroundLastName() throws Exception {
    Author expected = new Author("Hans", "H.", null, "van den Bergen", null);
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("{van den Bergen}, Hans")
    );
  }

  @Test
  public void parseNameWithHyphenInFirstName() throws Exception {
    Author expected = new Author("Tse-tung", "T.-t.", null, "Mao", null);
    assertEquals(AuthorList.of(expected), AuthorList.parse("Tse-tung Mao"));
  }

  @Test
  public void parseNameWithHyphenInLastName() throws Exception {
    Author expected = new Author("Firstname", "F.", null, "Bailey-Jones", null);
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("Firstname Bailey-Jones")
    );
  }

  @Test
  public void parseNameWithHyphenInLastNameWithInitials() throws Exception {
    Author expected = new Author("E. S.", "E. S.", null, "El-{M}allah", null);
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("E. S. El-{M}allah")
    );
  }

  @Test
  public void parseNameWithHyphenInLastNameWithEscaped() throws Exception {
    Author expected = new Author(
      "E. S.",
      "E. S.",
      null,
      "{K}ent-{B}oswell",
      null
    );
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("E. S. {K}ent-{B}oswell")
    );
  }

  @Test
  public void parseNameWithHyphenInLastNameWhenLastNameGivenFirst()
    throws Exception {
    Author expected = new Author("ʿAbdallāh", "ʿ.", null, "al-Ṣāliḥ", null);
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("al-Ṣāliḥ, ʿAbdallāh")
    );
  }

  @Test
  public void parseNameWithBraces() throws Exception {
    Author expected = new Author("H{e}lene", "H.", null, "Fiaux", null);
    assertEquals(AuthorList.of(expected), AuthorList.parse("H{e}lene Fiaux"));
  }

  @Test
  public void parseFirstNameFromFirstAuthorMultipleAuthorsWithLatexNames()
    throws Exception {
    assertEquals(
      "Mu{\\d{h}}ammad",
      AuthorList.parse(
        "Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm"
      )
        .getAuthor(0)
        .getGivenName()
        .orElse(null)
    );
  }

  @Test
  public void parseFirstNameFromSecondAuthorMultipleAuthorsWithLatexNames()
    throws Exception {
    assertEquals(
      "Corrado",
      AuthorList.parse(
        "Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm"
      )
        .getAuthor(1)
        .getGivenName()
        .orElse(null)
    );
  }

  @Test
  public void parseLastNameFromFirstAuthorMultipleAuthorsWithLatexNames()
    throws Exception {
    assertEquals(
      "al-Khw{\\={a}}rizm{\\={i}}",
      AuthorList.parse(
        "Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm"
      )
        .getAuthor(0)
        .getFamilyName()
        .orElse(null)
    );
  }

  @Test
  public void parseLastNameFromSecondAuthorMultipleAuthorsWithLatexNames()
    throws Exception {
    assertEquals(
      "B{\\\"o}hm",
      AuthorList.parse(
        "Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm"
      )
        .getAuthor(1)
        .getFamilyName()
        .orElse(null)
    );
  }

  @Test
  public void parseInstitutionAuthorWithLatexNames() throws Exception {
    assertEquals(
      "The Ban\\={u} M\\={u}s\\={a} brothers",
      AuthorList.parse("{The Ban\\={u} M\\={u}s\\={a} brothers}")
        .getAuthor(0)
        .getFamilyName()
        .orElse(null)
    );
  }

  @Test
  public void parseRetrieveCachedAuthorListAfterGarbageCollection()
    throws Exception {
    final String uniqueAuthorName = "Osvaldo Iongi";
    AuthorList author = AuthorList.parse(uniqueAuthorName);
    System.gc();
    assertSame(author, AuthorList.parse(uniqueAuthorName));
  }

  @Test
  public void parseGarbageCollectAuthorListForUnreachableKey()
    throws Exception {
    final String uniqueAuthorName = "Fleur Hornbach";

    AuthorList uniqueAuthor = AuthorList.parse(new String(uniqueAuthorName));
    System.gc();
    assertNotSame(uniqueAuthor, AuthorList.parse(uniqueAuthorName));
  }

  @Test
  public void parseGarbageCollectUnreachableInstitution() throws Exception {
    final String uniqueInstitutionName = "{Unique LLC}";

    AuthorList uniqueInstitution = AuthorList.parse(
      new String(uniqueInstitutionName)
    );
    System.gc();
    assertNotSame(uniqueInstitution, AuthorList.parse(uniqueInstitutionName));
  }

  @Test
  public void parseCacheAuthorsWithTwoOrMoreCommasAndWithSpaceInAllParts()
    throws Exception {
    final String uniqueAuthorsNames =
      "Basil Dankworth, Gianna Birdwhistle, Cosmo Berrycloth";
    AuthorList uniqueAuthors = AuthorList.parse(uniqueAuthorsNames);
    System.gc();
    assertSame(uniqueAuthors, AuthorList.parse(uniqueAuthorsNames));
  }

  @Test
  public void parseCacheAuthorsWithTwoOrMoreCommasAndWithoutSpaceInAllParts()
    throws Exception {
    final String uniqueAuthorsNames = "Dankworth, Jr., Braelynn";
    AuthorList uniqueAuthors = AuthorList.parse(uniqueAuthorsNames);
    System.gc();
    assertSame(uniqueAuthors, AuthorList.parse(uniqueAuthorsNames));
  }

  @Test
  public void correctNamesWithOneComma() throws Exception {
    Author expected = new Author(
      "Alexander der Große",
      "A. d. G.",
      null,
      "Canon der Barbar",
      null
    );
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("Canon der Barbar, Alexander der Große")
    );

    expected = new Author(
      "Alexander H. G.",
      "A. H. G.",
      null,
      "Rinnooy Kan",
      null
    );
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("Rinnooy Kan, Alexander H. G.")
    );

    expected = new Author(
      "Alexander Hendrik George",
      "A. H. G.",
      null,
      "Rinnooy Kan",
      null
    );
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("Rinnooy Kan, Alexander Hendrik George")
    );

    expected = new Author(
      "José María",
      "J. M.",
      null,
      "Rodriguez Fernandez",
      null
    );
    assertEquals(
      AuthorList.of(expected),
      AuthorList.parse("Rodriguez Fernandez, José María")
    );
  }

  @Test
  public void equalsFalseDifferentOrder() {
    Author firstAuthor = new Author("A", null, null, null, null);
    Author secondAuthor = new Author("B", null, null, null, null);
    AuthorList firstAuthorList = AuthorList.of(firstAuthor, secondAuthor);
    AuthorList secondAuthorList = AuthorList.of(secondAuthor, firstAuthor);
    assertNotEquals(firstAuthorList, secondAuthorList);
  }

  @Test
  public void equalsFalseWhenNotAuthorList() {
    assertNotEquals(
      AuthorList.of(new Author(null, null, null, null, null)),
      new Author(null, null, null, null, null)
    );
  }

  @Test
  public void equalsTrueReflexive() {
    AuthorList authorList = AuthorList.of(
      new Author(null, null, null, null, null)
    );
    assertEquals(authorList, authorList);
  }

  @Test
  public void equalsTrueSymmetric() {
    AuthorList firstAuthorList = AuthorList.of(
      new Author("A", null, null, null, null)
    );
    AuthorList secondAuthorList = AuthorList.of(
      new Author("A", null, null, null, null)
    );
    assertEquals(firstAuthorList, secondAuthorList);
    assertEquals(secondAuthorList, firstAuthorList);
  }

  @Test
  public void equalsTrueTransitive() {
    AuthorList firstAuthorList = AuthorList.of(
      new Author("A", null, null, null, null)
    );
    AuthorList secondAuthorList = AuthorList.of(
      new Author("A", null, null, null, null)
    );
    AuthorList thirdAuthorList = AuthorList.of(
      new Author("A", null, null, null, null)
    );
    assertEquals(firstAuthorList, secondAuthorList);
    assertEquals(secondAuthorList, thirdAuthorList);
    assertEquals(firstAuthorList, thirdAuthorList);
  }

  @Test
  public void equalsTrueConsistent() {
    AuthorList firstAuthorList = AuthorList.of(
      new Author("A", null, null, null, null)
    );
    AuthorList secondAuthorList = AuthorList.of(
      new Author("A", null, null, null, null)
    );
    assertEquals(firstAuthorList, secondAuthorList);
    assertEquals(firstAuthorList, secondAuthorList);
    assertEquals(firstAuthorList, secondAuthorList);
  }

  @Test
  public void equalsFalseForNull() {
    assertNotEquals(
      null,
      AuthorList.of(new Author(null, null, null, null, null))
    );
  }

  @Test
  public void hashCodeConsistent() {
    AuthorList authorList = AuthorList.of(
      new Author(null, null, null, null, null)
    );
    assertEquals(authorList.hashCode(), authorList.hashCode());
  }

  @Test
  public void hashCodeNotConstant() {
    AuthorList firstAuthorList = AuthorList.of(
      new Author("A", null, null, null, null)
    );
    AuthorList secondAuthorList = AuthorList.of(
      new Author("B", null, null, null, null)
    );
    assertNotEquals(firstAuthorList.hashCode(), secondAuthorList.hashCode());
  }

  @Test
  public void getAsLastFirstFirstLastNamesWithAndEmptyAuthor() {
    assertEquals("", EMPTY_AUTHOR.getAsLastFirstFirstLastNamesWithAnd(true));
  }

  @Test
  public void getAsLastFirstFirstLastNamesWithAndMultipleAuthors() {
    assertEquals(
      "al-Khw{\\={a}}rizm{\\={i}}, M. and C. B{\\\"o}hm and K. G{\\\"{o}}del",
      THREE_AUTHORS_WITH_LATEX.getAsLastFirstFirstLastNamesWithAnd(true)
    );
  }

  @Test
  public void testGetAsLastNamesWithAnd() {
    assertEquals(
      "Doe and Smith",
      AuthorList.parse("John Doe and Jane Smith").getAsLastNames(true)
    );
  }

  @Test
  public void testGetAsLastFirstFirstLastNamesWithoutAndEmptyAuthor() {
    assertEquals("", EMPTY_AUTHOR.getAsLastFirstFirstLastNamesWithAnd(false));
  }

  @Test
  public void parseAuthorWithoutMiddleName() {
    Author expected = new Author("John", "J.", null, "Doe", null);
    assertEquals(AuthorList.of(expected), AuthorList.parse("John Doe"));
  }

  @Test
  public void parseAuthorWithoutSuffix() {
    Author expected = new Author("John", "J.", null, "Doe", null);
    assertEquals(AuthorList.of(expected), AuthorList.parse("John Doe"));
  }

  @Test
  public void parseAuthorWithoutMiddleNameAndSuffix() {
    Author expected = new Author("John", "J.", null, "Doe", null);
    assertEquals(AuthorList.of(expected), AuthorList.parse("John Doe"));
  }

  @Test
  public void testGetAsLastNamesWithAndSingleAuthor() {
    assertEquals("Doe", AuthorList.parse("John Doe").getAsLastNames(true));
  }

  @Test
  public void testGetAsLastNamesWithoutAndSingleAuthor() {
    assertEquals("Doe", AuthorList.parse("John Doe").getAsLastNames(false));
  }

  @Test
  public void parseAuthorWithoutMiddleNameAndPrefix() {
    Author expected = new Author("John", "J.", null, "Doe", null);
    assertEquals(AuthorList.of(expected), AuthorList.parse("John Doe"));
  }

  @Test
  public void testGetAsLastNamesWithoutAndMultipleAuthors() {
    assertEquals("Doe and Smith", authorList.getAsLastNames(true));
  }

  @Test
  public void testGetAsFirstLastNamesWithAbbreviationAndOxfordComma() {
    assertEquals(
      "J. Doe and J. Smith",
      authorList.getAsFirstLastNames(true, true)
    );
  }

  @Test
  public void testGetAsFirstLastNamesWithoutAbbreviationAndWithoutOxfordComma() {
    assertEquals(
      "John Doe and Jane Smith",
      authorList.getAsFirstLastNames(false, false)
    );
  }
}
