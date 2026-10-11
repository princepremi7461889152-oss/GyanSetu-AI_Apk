package com.example.data

import com.example.model.ChapterEntity

object SampleNotesData {
    fun getInitialChapters(): List<ChapterEntity> {
        return listOf(
            ChapterEntity(
                chapterNumber = 1,
                title = "प्रकाश संश्लेषण (Photosynthesis)",
                subject = "Science (Biology)",
                content = """
प्रकाश संश्लेषण (Photosynthesis) वह रासायनिक प्रक्रिया है जिसके द्वारा हरे पौधे सूर्य के प्रकाश, जल (H2O) और कार्बन डाइऑक्साइड (CO2) का उपयोग करके अपना भोजन (ग्लूकोज - C6H12O6) और ऑक्सीजन (O2) बनाते हैं।

मुख्य समीकरण:
6CO2 + 6H2O + Sunlight + Chlorophyll → C6H12O6 + 6O2

महत्वपूर्ण घटक:
1. क्लोरोफिल (Chlorophyll): पत्तियों में मौजूद हरा वर्णक जो सौर ऊर्जा को अवशोषित करता है।
2. स्टोमेटा (Stomata): पत्तियों की निचली सतह पर छोटे छिद्र जिनके माध्यम से CO2 अंदर आती है और O2 बाहर निकलती है।
3. जाइलम (Xylem): जड़ों से पत्तियों तक पानी पहुंचाने वाली संवहनी नली।
4. फ्लोएम (Phloem): पत्तियों में बने भोजन को पौधे के अन्य भागों तक ले जाने वाली नली।

दो मुख्य चरण:
- Light Reaction (प्रकाश निर्भर अभिक्रिया): थाइलाकोइड में होती है, जल का विभाजन होता है और ATP बनता है।
- Dark Reaction / Calvin Cycle (प्रकाश स्वतंत्र अभिक्रिया): स्ट्रोमा में होती है, CO2 से ग्लूकोज बनता है।
                """.trimIndent(),
                isLocked = false,
                summaryJson = """Photosynthesis wo process hai jisme plants dhoop (sunlight), paani aur CO2 use karke glucose aur oxygen banate hain. Chlorophyll dhoop absorb karta hai aur Stomata gas exchange karta hai.""",
                flashcardsJson = """[{"id":1,"question":"What is Photosynthesis?","answer":"Plants use sunlight, CO2, and water to make glucose and release oxygen.","memoryTrick":"Sun + Water + Air = Plant's Kitchen Food!"},{"id":2,"question":"What is the role of Chlorophyll?","answer":"Traps sunlight in green leaves for photosynthesis.","memoryTrick":"Chilgoza green pigment"}]"""
            ),
            ChapterEntity(
                chapterNumber = 2,
                title = "विद्युत धारा और ओम का नियम (Electric Current & Ohm's Law)",
                subject = "Physics",
                content = """
विद्युत धारा (Electric Current):
आवेश के प्रवाह की दर को विद्युत धारा कहते हैं। (Rate of flow of electric charge).
सूत्र: I = Q / t
इकाई: एम्पीयर (Ampere - A)

ओम का नियम (Ohm's Law):
यदि किसी चालक की भौतिक अवस्थाएं (जैसे तापमान) स्थिर रहें, तो चालक के सिरों पर लगाया गया विभवांतर (Voltage - V) उसमें प्रवाहित धारा (I) के समानुपाती होता है।
सूत्र: V = I × R
जहाँ:
- V = विभवांतर (Volts)
- I = धारा (Amperes)
- R = प्रतिरोध (Resistance - Ohms, Ω)

प्रतिरोधकता (Resistivity - ρ):
R = ρ × (L / A)
- L = चालक की लंबाई (लंबाई बढ़ने पर प्रतिरोध बढ़ता है)
- A = अनुप्रस्थ काट का क्षेत्रफल (तार मोटा होने पर प्रतिरोध घटता है)

प्रतिरोधों का संयोजन:
1. श्रेणी क्रम (Series): R_total = R1 + R2 + R3
2. समांतर क्रम (Parallel): 1/R_total = 1/R1 + 1/R2 + 1/R3
                """.trimIndent(),
                isLocked = false
            ),
            ChapterEntity(
                chapterNumber = 3,
                title = "गुप्त परीक्षा नोट्स: अम्ल, क्षार और लवण (Acids, Bases & Salts)",
                subject = "Chemistry (Exam Secret Notes)",
                content = """
अम्ल (Acids):
- स्वाद में खट्टे होते हैं।
- नीले लिटमस को लाल कर देते हैं।
- जलीय विलयन में H+ आयन देते हैं।
- pH मान 7 से कम होता है (0 to 6.9)।
उदाहरण: हाइड्रोक्लोरिक एसिड (HCl), सल्फ्यूरिक एसिड (H2SO4)।

क्षार (Bases):
- स्वाद में कड़वे और स्पर्श में साबुन जैसे चिकने होते हैं।
- लाल लिटमस को नीला कर देते हैं।
- जलीय विलयन में OH- आयन देते हैं।
- pH मान 7 से अधिक होता है (7.1 to 14)।
उदाहरण: सोडियम हाइड्रॉक्साइड (NaOH), कैल्शियम हाइड्रॉक्साइड (Ca(OH)2)।

उदासीनीकरण अभिक्रिया (Neutralization Reaction):
Acid + Base → Salt + Water + Heat
HCl + NaOH → NaCl + H2O

pH पैमाना (pH Scale):
- 0 to 14 तक मापा जाता है।
- pH 7 = उदासीन (Neutral - शुद्ध जल)
- pH < 7 = अम्लीय (Acidic)
- pH > 7 = क्षारीय (Basic)
                """.trimIndent(),
                isLocked = true,
                passwordHash = "1234",
                passwordHint = "सरल 4 अंकों का पिन: 1234"
            )
        )
    }

    // Quick templates to auto-fill up to 6 chapters in the Batch Upload screen
    fun get6ChapterBatchPreset(): List<Triple<String, String, String>> {
        return listOf(
            Triple(
                "Chapter 1: न्यूटन के गति के नियम (Newton's Laws)",
                "Physics",
                "पहला नियम (जड़त्व का नियम - Law of Inertia): कोई वस्तु तब तक स्थिर रहेगी जब तक उस पर बाहरी बल न लगे।\nदूसरा नियम: F = m × a (बल = द्रव्यमान × त्वरण)।\nतीसरा नियम: प्रत्येक क्रिया की बराबर और विपरीत प्रतिक्रिया होती है (Action-Reaction)."
            ),
            Triple(
                "Chapter 2: रासायनिक अभिक्रियाएं (Chemical Reactions)",
                "Chemistry",
                "संयोजन अभिक्रिया: A + B → AB\nवियोजन अभिक्रिया: AB → A + B\nविस्थापन अभिक्रिया: Zn + CuSO4 → ZnSO4 + Cu\nऑक्सीकरण (Oxidation): ऑक्सीजन का जुड़ना या हाइड्रोजन का निकलना।\nअपचयन (Reduction): हाइड्रोजन का जुड़ना या ऑक्सीजन का निकलना।"
            ),
            Triple(
                "Chapter 3: कोशिका - जीवन की इकाई (The Cell)",
                "Biology",
                "कोशिका जीवन की सबसे छोटी संरचनात्मक और कार्यात्मक इकाई है। रॉबर्ट हुक ने 1665 में खोज की। माइटोकॉन्ड्रिया कोशिका का 'Powerhouse' (ऊर्जा घर) कहलाता है क्योंकि यह ATP उत्पन्न करता है।"
            ),
            Triple(
                "Chapter 4: त्रिकोणमिति के मूल सूत्र (Trigonometry Basics)",
                "Mathematics",
                "sin²θ + cos²θ = 1\n1 + tan²θ = sec²θ\n1 + cot²θ = cosec²θ\nLAL/KKA ट्रिक: लम्ब/कर्ण = sin, आधार/कर्ण = cos, लम्ब/आधार = tan।"
            ),
            Triple(
                "Chapter 5: 1857 की क्रांति (1857 Revolt)",
                "History",
                "भारत का पहला स्वतंत्रता संग्राम। 10 मई 1857 को मेरठ से शुरू हुआ। मंगल पांडे ने बैरकपुर में चर्बी वाले कारतूसों का विरोध किया। बहादुर शाह जफर को क्रांति का नेता घोषित किया गया।"
            ),
            Triple(
                "Chapter 6: भारतीय संविधान की प्रस्तावना (Indian Constitution)",
                "Civics / Polity",
                "संविधान सभा के अध्यक्ष डॉ. राजेन्द्र प्रसाद और प्रारूप समिति के अध्यक्ष डॉ. भीमराव अंबेडकर थे। 26 जनवरी 1950 को लागू हुआ। भारत एक संप्रभु, समाजवादी, धर्मनिरपेक्ष, लोकतांत्रिक गणराज्य है।"
            )
        )
    }
}
