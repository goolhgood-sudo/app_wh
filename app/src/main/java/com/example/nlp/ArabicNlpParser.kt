package com.example.nlp

import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import java.util.Calendar

data class ParsedTaskResult(
    val cleanTitle: String,
    val dueDate: Long?,
    val priority: TaskPriority,
    val category: TaskCategory,
    val matchedDateSummary: String? = null
)

object ArabicNlpParser {

    fun parse(input: String): ParsedTaskResult {
        var text = input.trim()
        if (text.isEmpty()) {
            return ParsedTaskResult(
                cleanTitle = "",
                dueDate = null,
                priority = TaskPriority.MEDIUM,
                category = TaskCategory.WORK
            )
        }

        // 1. Detect Priority
        var detectedPriority = TaskPriority.MEDIUM
        val highPriorityRegex = Regex("(عاجل|مهم جداً|مهم جدا|ضروري|أولوية قصوى|أولوية عالية|هام جدا|هام)", RegexOption.IGNORE_CASE)
        val lowPriorityRegex = Regex("(غير عاجل|أولوية منخفضة|غير مهم|عادي)", RegexOption.IGNORE_CASE)

        if (highPriorityRegex.containsMatchIn(text)) {
            detectedPriority = TaskPriority.HIGH
            text = highPriorityRegex.replace(text, "").trim()
        } else if (lowPriorityRegex.containsMatchIn(text)) {
            detectedPriority = TaskPriority.LOW
            text = lowPriorityRegex.replace(text, "").trim()
        }

        // 2. Detect Category
        var detectedCategory = TaskCategory.WORK
        val studyRegex = Regex("(مذاكرة|دراسة|امتحان|اختبار|واجب|جامعة|مدرسة|كتاب|بحث|كورس)", RegexOption.IGNORE_CASE)
        val personalRegex = Regex("(شخصي|طبيب|دكتور|صيدلية|رياضة|تمرين|جيم|تسوق|سوبرماركت|بيت|عائلة)", RegexOption.IGNORE_CASE)
        val workRegex = Regex("(عمل|شغل|وظيفة|اجتماع|مشروع|عميل|تقرير|بريد|إيميل)", RegexOption.IGNORE_CASE)

        if (studyRegex.containsMatchIn(text)) {
            detectedCategory = TaskCategory.STUDY
        } else if (personalRegex.containsMatchIn(text)) {
            detectedCategory = TaskCategory.PERSONAL
        } else if (workRegex.containsMatchIn(text)) {
            detectedCategory = TaskCategory.WORK
        }

        // 3. Detect Due Date & Time
        var calendar = Calendar.getInstance()
        var dateDetected = false
        var timeDetected = false
        var matchedSummaryParts = mutableListOf<String>()

        // A. Relative offset (e.g., بعد ساعة / بعد ساعتين / بعد 3 ساعات / بعد 30 دقيقة)
        val afterMinutesRegex = Regex("بعد\\s+(\\d+)\\s*(دقيقة|دقائق)", RegexOption.IGNORE_CASE)
        val afterMinutesMatch = afterMinutesRegex.find(text)
        if (afterMinutesMatch != null) {
            val minutes = afterMinutesMatch.groupValues[1].toIntOrNull() ?: 15
            calendar.add(Calendar.MINUTE, minutes)
            dateDetected = true
            timeDetected = true
            matchedSummaryParts.add("بعد $minutes دقيقة")
            text = text.replace(afterMinutesMatch.value, " ").trim()
        }

        val afterHoursRegex = Regex("بعد\\s+(\\d+)\\s*(ساعة|ساعات)", RegexOption.IGNORE_CASE)
        val afterHoursMatch = afterHoursRegex.find(text)
        if (!timeDetected && afterHoursMatch != null) {
            val hours = afterHoursMatch.groupValues[1].toIntOrNull() ?: 1
            calendar.add(Calendar.HOUR_OF_DAY, hours)
            dateDetected = true
            timeDetected = true
            matchedSummaryParts.add("بعد $hours ساعة")
            text = text.replace(afterHoursMatch.value, " ").trim()
        }

        if (!timeDetected) {
            if (text.contains("بعد ساعتين") || text.contains("بعد ساعتان")) {
                calendar.add(Calendar.HOUR_OF_DAY, 2)
                dateDetected = true
                timeDetected = true
                matchedSummaryParts.add("بعد ساعتين")
                text = text.replace(Regex("بعد\\s+(ساعتين|ساعتان)"), " ").trim()
            } else if (text.contains("بعد ساعة")) {
                calendar.add(Calendar.HOUR_OF_DAY, 1)
                dateDetected = true
                timeDetected = true
                matchedSummaryParts.add("بعد ساعة")
                text = text.replace(Regex("بعد\\s+ساعة"), " ").trim()
            } else if (text.contains("بعد نصف ساعة") || text.contains("بعد نص ساعة")) {
                calendar.add(Calendar.MINUTE, 30)
                dateDetected = true
                timeDetected = true
                matchedSummaryParts.add("بعد 30 دقيقة")
                text = text.replace(Regex("بعد\\s+(نصف|نص)\\s+ساعة"), " ").trim()
            }
        }

        // B. Day tokens (اليوم، غداً، بعد غد، أسماء الأيام)
        if (text.contains("بعد غد") || text.contains("بعد غداً") || text.contains("بعد بكرة")) {
            calendar.add(Calendar.DAY_OF_YEAR, 2)
            dateDetected = true
            matchedSummaryParts.add("بعد غد")
            text = text.replace(Regex("(بعد\\s+غداً|بعد\\s+غد|بعد\\s+بكرة)"), " ").trim()
        } else if (text.contains("غداً") || text.contains("غدا") || text.contains("بكرة") || text.contains("باكر")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateDetected = true
            matchedSummaryParts.add("غداً")
            text = text.replace(Regex("(غداً|غدا|بكرة|باكر)"), " ").trim()
        } else if (text.contains("اليوم") || text.contains("الليلة")) {
            dateDetected = true
            matchedSummaryParts.add("اليوم")
            text = text.replace(Regex("(اليوم|الليلة)"), " ").trim()
        } else {
            // Check day of week
            val daysMap = mapOf(
                "السبت" to Calendar.SATURDAY,
                "الأحد" to Calendar.SUNDAY,
                "الاحد" to Calendar.SUNDAY,
                "الاثنين" to Calendar.MONDAY,
                "الإثنين" to Calendar.MONDAY,
                "الثلاثاء" to Calendar.TUESDAY,
                "الأربعاء" to Calendar.WEDNESDAY,
                "الاربعاء" to Calendar.WEDNESDAY,
                "الخميس" to Calendar.THURSDAY,
                "الجمعة" to Calendar.FRIDAY
            )
            for ((dayName, dayConstant) in daysMap) {
                val dayRegex = Regex("(يوم\\s+$dayName|$dayName)", RegexOption.IGNORE_CASE)
                if (dayRegex.containsMatchIn(text)) {
                    val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                    var daysUntil = (dayConstant - currentDay + 7) % 7
                    if (daysUntil == 0) daysUntil = 7
                    calendar.add(Calendar.DAY_OF_YEAR, daysUntil)
                    dateDetected = true
                    matchedSummaryParts.add("يوم $dayName")
                    text = dayRegex.replace(text, " ").trim()
                    break
                }
            }
        }

        // C. Explicit Time of day
        // Named prayer/day times
        val namedTimes = mapOf(
            "الظهر" to Pair(12, 30),
            "العصر" to Pair(15, 30),
            "المغرب" to Pair(18, 0),
            "العشاء" to Pair(19, 30),
            "الفجر" to Pair(5, 0),
            "الصباح" to Pair(9, 0),
            "المساء" to Pair(18, 0)
        )
        for ((name, timePair) in namedTimes) {
            val nameRegex = Regex("(وقت\\s+$name|في\\s+$name|$name)", RegexOption.IGNORE_CASE)
            if (!timeDetected && nameRegex.containsMatchIn(text)) {
                calendar.set(Calendar.HOUR_OF_DAY, timePair.first)
                calendar.set(Calendar.MINUTE, timePair.second)
                calendar.set(Calendar.SECOND, 0)
                timeDetected = true
                dateDetected = true
                matchedSummaryParts.add(name)
                text = nameRegex.replace(text, " ").trim()
                break
            }
        }

        // Numbered time: e.g. "الساعة 5 مساءً" or "5:30 م" or "الساعة 10 صباحاً"
        val timeRegex = Regex("(?:الساعة\\s*)?(\\d{1,2})(?::(\\d{2}))?\\s*(صباحاً|صباحا|مساءً|مساء|ص|م)?", RegexOption.IGNORE_CASE)
        val timeMatch = timeRegex.find(text)
        if (!timeDetected && timeMatch != null && timeMatch.value.trim().isNotEmpty()) {
            val rawHour = timeMatch.groupValues[1].toIntOrNull()
            val rawMinute = timeMatch.groupValues[2].toIntOrNull() ?: 0
            val period = timeMatch.groupValues[3].trim()

            if (rawHour != null && rawHour in 1..24) {
                var hour = rawHour
                if (period.contains("م") || period.contains("مساء")) {
                    if (hour < 12) hour += 12
                } else if (period.contains("ص") || period.contains("صباح")) {
                    if (hour == 12) hour = 0
                } else if (hour in 1..7) {
                    // Default afternoon assumption for hours 1-7 if unspecified in work tasks
                    hour += 12
                }

                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, rawMinute)
                calendar.set(Calendar.SECOND, 0)
                timeDetected = true
                dateDetected = true
                matchedSummaryParts.add(timeMatch.value.trim())
                text = text.replace(timeMatch.value, " ").trim()
            }
        }

        // If date detected but no time, default to 9:00 AM next day or current
        if (dateDetected && !timeDetected) {
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
        }

        val dueDate = if (dateDetected) calendar.timeInMillis else null
        val matchedSummary = if (matchedSummaryParts.isNotEmpty()) matchedSummaryParts.joinToString("، ") else null

        // Clean extra punctuation and spaces from title
        val cleanedTitle = text
            .replace(Regex("^[،,.-]+|[،,.-]+$"), "")
            .replace(Regex("\\s{2,}"), " ")
            .trim()

        return ParsedTaskResult(
            cleanTitle = if (cleanedTitle.isNotEmpty()) cleanedTitle else input.trim(),
            dueDate = dueDate,
            priority = detectedPriority,
            category = detectedCategory,
            matchedDateSummary = matchedSummary
        )
    }
}
