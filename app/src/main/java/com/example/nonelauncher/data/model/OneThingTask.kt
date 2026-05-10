package com.example.nonelauncher.data.model

data class SubTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false
)

data class OneThingTask(
    val mainTask: String,
    val subTasks: List<SubTask>
)

fun serializeOneThingTask(task: OneThingTask): String {
    val subtaskStr = task.subTasks.joinToString(";") { sub ->
        val escapedText = sub.text
            .replace("\\", "\\\\")
            .replace("|", "\\|")
            .replace(";", "\\;")
            .replace(",", "\\,")
        "${sub.id},$escapedText,${sub.isCompleted}"
    }
    return "${task.mainTask}|$subtaskStr"
}

fun parseOneThingTaskWithState(serialized: String?): OneThingTask? {
    if (serialized.isNullOrEmpty()) return null
    val parts = serialized.split("|")
    if (parts.size < 2) return null

    val mainTask = parts[0]
    val subTasks = parts[1].split(";").filter { it.isNotEmpty() }.mapNotNull { subStr ->
        val subParts = subStr.split(",")
        if (subParts.size >= 3) {
            SubTask(
                id = subParts[0],
                text = subParts[1].replace("\\|", "|").replace("\\;", ";").replace("\\,", ","),
                isCompleted = subParts[2].toBoolean()
            )
        } else null
    }

    return OneThingTask(mainTask, subTasks)
}

fun parseOneThingTask(serialized: String?): OneThingTask? {
    if (serialized.isNullOrEmpty()) return null
    val pipeIndex = serialized.indexOf('|')
    if (pipeIndex == -1) return null

    val mainTask = serialized.substring(0, pipeIndex)
    val subtaskStr = serialized.substring(pipeIndex + 1)

    val subTasks = subtaskStr.split(";").filter { it.isNotEmpty() }.mapNotNull { part ->
        val segments = part.split(",")
        if (segments.size >= 3) {
            SubTask(
                id = segments[0],
                text = segments[1]
                    .replace("\\,", ",")
                    .replace("\\;", ";")
                    .replace("\\|", "|")
                    .replace("\\\\", "\\"),
                isCompleted = segments[2].toBoolean()
            )
        } else null
    }

    return OneThingTask(mainTask, subTasks)
}
