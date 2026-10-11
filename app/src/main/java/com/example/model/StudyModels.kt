package com.example.model

enum class LanguageStyle(val label: String, val promptInstruction: String) {
    HINGLISH("Hinglish (सरल बोलचाल)", "Use clear, casual conversational Hinglish (Hindi written in Roman/English alphabet mixed with common Hindi words) so Indian students can grasp concepts instantly."),
    HINDI("शुद्ध हिन्दी (Hindi)", "Use easy-to-understand Hindi in Devanagari script."),
    ENGLISH("Simple English", "Use ultra-simple, plain English without academic jargon.")
}

enum class ExplanationLevel(val label: String, val description: String) {
    ELI5("कक्षा 5 के बच्चे जैसा (ELI5)", "Explain like teaching a 10-year-old child using real-life toys, kitchen, cricket, or household analogies."),
    REAL_WORLD("रोज़मर्रा का उदाहरण (Analogy)", "Use a vivid real-life daily Indian life scenario (like Chai brewing, street traffic, mobile battery, cooking) to explain the principle."),
    EXAM_STEPS("एग्जाम स्टेप-बाय-स्टेप (Exam Ready)", "Step-by-step breakdown with definition, formula, core steps, and 2-sentence conclusion for full marks in exams.")
}

data class HardQuestionExplanation(
    val question: String,
    val simpleConcept: String,
    val realLifeAnalogy: String,
    val stepByStepBreakdown: List<String>,
    val quickExamSummary: String,
    val proTip: String
)

data class StudyFlashcard(
    val id: Int,
    val question: String,
    val answer: String,
    val memoryTrick: String = ""
)

data class StudyQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ChapterSummary(
    val overview: String,
    val keyPoints: List<String>,
    val importantFormulasOrDefinitions: List<String>,
    val examWarningOrTrap: String = ""
)

data class QuestionMistakeAnalysis(
    val question: String,
    val studentAttempt: String,
    val detectedMistake: String,
    val mistakeType: String,
    val correctSolution: List<String>,
    val keyFormulas: List<String>,
    val proTipToAvoid: String
)
