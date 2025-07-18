package core;
import java.util.Map;
import java.util.HashMap;

public class MoodAnalyzer {
    private Map<String, String[]> moodKeywords;
    
    public MoodAnalyzer() {
        initializeMoodKeywords();
    }
    
    private void initializeMoodKeywords() {
        moodKeywords = new HashMap<>();
        moodKeywords.put("happy", new String[]{"happy", "joy", "excited", "great", "amazing", "wonderful", "fantastic", "awesome", "cheerful", "delighted", "thrilled", "ecstatic", "upbeat", "positive", "good", "excellent"});
        moodKeywords.put("sad", new String[]{"sad", "unhappy", "down", "depressed", "melancholy", "blue", "gloomy", "disappointed", "heartbroken", "miserable", "dejected", "despondent", "sorrowful", "grief", "crying", "tears"});
        moodKeywords.put("angry", new String[]{"angry", "mad", "furious", "rage", "irritated", "annoyed", "frustrated", "outraged", "livid", "enraged", "pissed", "upset", "hate", "disgusted"});
        moodKeywords.put("calm", new String[]{"calm", "peaceful", "relaxed", "serene", "tranquil", "quiet", "still", "zen", "meditative", "restful", "soothing", "gentle", "mellow", "chill"});
        moodKeywords.put("energetic", new String[]{"energetic", "pumped", "hyper", "active", "dynamic", "vigorous", "lively", "animated", "spirited", "bouncy", "enthusiastic", "motivated", "driven"});
        moodKeywords.put("love", new String[]{"love", "romantic", "heart", "adore", "affection", "romance", "caring", "tender", "sweet", "crush", "relationship", "valentine", "beloved", "passion"});
    }
    
    public MoodResult analyzeMoodWithConfidence(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new MoodResult("neutral", 0.5);
        }
        
        text = text.toLowerCase();
        String detectedMood = "neutral";
        double maxScore = 0;
        
        // Calculate scores for each mood
        for (Map.Entry<String, String[]> entry : moodKeywords.entrySet()) {
            String mood = entry.getKey();
            String[] keywords = entry.getValue();
            
            double score = calculateMoodScore(text, keywords);
            if (score > maxScore) {
                maxScore = score;
                detectedMood = mood;
            }
        }
        
        // Normalize confidence (0.0 to 1.0)
        double confidence = Math.min(maxScore / 10.0, 1.0);
        if (confidence < 0.3) {
            detectedMood = "neutral";
            confidence = 0.5;
        }
        
        return new MoodResult(detectedMood, confidence);
    }
    
    private double calculateMoodScore(String text, String[] keywords) {
        double score = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                // Weight longer keywords more heavily
                score += keyword.length() * 0.5 + 1;
                
                // Bonus for exact word matches
                if (text.matches(".*\\b" + keyword + "\\b.*")) {
                    score += 2;
                }
            }
        }
        return score;
    }
    
    // Legacy method for compatibility
    public String predictMood(String userInput) {
        return analyzeMoodWithConfidence(userInput).emotion;
    }
}

// Result class for mood analysis
class MoodResult {
    public final String emotion;
    public final double confidence;
    
    public MoodResult(String emotion, double confidence) {
        this.emotion = emotion;
        this.confidence = confidence;
    }
}
