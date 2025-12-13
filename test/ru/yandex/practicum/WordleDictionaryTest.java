package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exception.InvalidWordException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.WordleDictionary.normalizeWord;

public class WordleDictionaryTest {

    private PrintWriter log = new PrintWriter(new StringWriter());

    private final WordleDictionary dictionary = new WordleDictionary(Arrays.asList(
            "банан", "герой", "гонец", "слово", "пчела",
            "банка", "абвгд", "горох", "горка", "слон", "второй"
    ), log);

    @Test
    public void testNormalizeWord() {
        assertEquals("банан", normalizeWord("БАНАН"));
        assertEquals("банан", normalizeWord("банан"));
        assertEquals("банае", normalizeWord("банаё"));
        assertEquals("банае", normalizeWord("БАНАЁ"));
        assertEquals("гонец", normalizeWord("гОнЁц"));
        assertEquals("банан", normalizeWord("  БАНАН  "));
        assertEquals("", normalizeWord(null));
    }

    @Test
    public void testContains() {
        assertTrue(dictionary.contains("Герой"));
        assertTrue(dictionary.contains("гЕрОй"));
        assertTrue(dictionary.contains("герой"));
        assertTrue(dictionary.contains("гОнЁц"));
        assertFalse(dictionary.contains("слоны"));
        assertFalse(dictionary.contains(null));
    }

    @Test
    public void testValidateWord() throws InvalidWordException {
        dictionary.validateWord("герой");
        dictionary.validateWord("ГЕРОЙ");
        dictionary.validateWord("гонец");

        assertThrows(InvalidWordException.class, () -> dictionary.validateWord("слон"));
        assertThrows(InvalidWordException.class, () -> dictionary.validateWord("длинное"));
        assertThrows(InvalidWordException.class, () -> dictionary.validateWord("hello"));
        assertThrows(InvalidWordException.class, () -> dictionary.validateWord(""));
        assertThrows(InvalidWordException.class, () -> dictionary.validateWord("12345"));
    }

    @Test
    public void testAnalyzeWord() {
        assertEquals("+++++", WordleDictionary.analyzeWord("герой", "герой"));
        assertEquals("+^-^-", WordleDictionary.analyzeWord("гонец", "герой"));
        assertEquals("^----", WordleDictionary.analyzeWord("абвгд", "герой"));
        assertEquals("+++++", WordleDictionary.analyzeWord("банан", "банан"));
        assertEquals("+++-^", WordleDictionary.analyzeWord("банан", "банка"));
        assertEquals("+++^-", WordleDictionary.analyzeWord("банка", "банан"));
        assertEquals("+++++", WordleDictionary.analyzeWord("ЁЖИКА", "ежика"));
        assertEquals("+++++", WordleDictionary.analyzeWord("ёжика", "ЕЖИКА"));

        assertThrows(IllegalArgumentException.class,
                () -> WordleDictionary.analyzeWord("длинное", "слово"));
        assertThrows(IllegalArgumentException.class,
                () -> WordleDictionary.analyzeWord("коротк", "слово"));
        assertThrows(IllegalArgumentException.class,
                () -> WordleDictionary.analyzeWord(null, "герой"));
        assertThrows(IllegalArgumentException.class,
                () -> WordleDictionary.analyzeWord("герой", null));
    }

    @Test
    public void testFindSuggestions() {
        Set<Character> mustContain = Set.of('г', 'о');
        Set<Character> mustNotContain = Set.of('й');
        String pattern = "г___о";

        List<String> suggestions = dictionary.findSuggestions(mustContain, mustNotContain, pattern);
        assertFalse(suggestions.contains("герой"));
        assertFalse(suggestions.contains("гонец"));
    }

    @Test
    public void testFindSuggestionsOptimized() {
        Map<Integer, Character> correctPositions = Map.of(0, 'г', 4, 'ц');
        Map<Integer, Set<Character>> wrongPositions = Map.of(1, Set.of('е'));

        List<String> suggestions = dictionary.findSuggestionsOptimized(
                Set.of('о'), Set.of(), correctPositions, wrongPositions);

        assertTrue(suggestions.contains("гонец"));
        assertFalse(suggestions.contains("герой"));
    }

    @Test
    public void testGetLetterFrequency() {
        Map<Character, Integer> freq = dictionary.getLetterFrequency();
        assertNotNull(freq.get('а'));
        assertNotNull(freq.get('о'));
        assertNotNull(freq.get('б'));
    }

    @Test
   public void testGetAllWords() {
        List<String> words = dictionary.getAllWords();
        assertEquals(9, words.size());
        assertTrue(words.contains("герой"));
        assertTrue(words.contains("банан"));
        assertTrue(words.contains("гонец"));
        assertTrue(words.contains("банка"));
        assertFalse(words.contains("слон"));
        assertFalse(words.contains("второй"));
    }

    @Test
    public void testGetRandomWord() {
        String randomWord = dictionary.getRandomWord();
        assertNotNull(randomWord);
        assertEquals(5, randomWord.length());
        assertTrue(dictionary.contains(randomWord));
    }

    @Test
   public void testSize() {
        assertEquals(9, dictionary.size());
    }
}