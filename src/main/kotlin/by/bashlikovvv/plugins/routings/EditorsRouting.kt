package by.bashlikovvv.plugins.routings

import by.bashlikovvv.api.dto.mappers.EditorMapper
import by.bashlikovvv.api.dto.request.UpdateEditorDto
import by.bashlikovvv.model.Editor
import by.bashlikovvv.model.Response
import by.bashlikovvv.services.EditorService
import by.bashlikovvv.util.getWithCheck
import by.bashlikovvv.util.respond
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.editorsRouting() {
    val editorsService: EditorService by inject()

    getEditors(editorsService)
    createEditor(editorsService)
    deleteEditorById(editorsService)
    getEditorById(editorsService)
    updateEditor(editorsService)
}

private fun Route.getEditors(editorsService: EditorService) {
    get("/api/v1.0/editors") {
        val editors = editorsService.getAll()

        respond(
            isCorrect = { editors.isNotEmpty() },
            onCorrect = { call.respond(status = HttpStatusCode.OK, editors) },
            onIncorrect = {
                call.respond(status = HttpStatusCode.OK, Response(HttpStatusCode.OK.value))
            }
        )
    }
}

private fun Route.createEditor(editorsService: EditorService) {
    post("/api/v1.0/editors") {
        val editor: UpdateEditorDto = call.receive()
        val addedEditor = getWithCheck { editorsService.create(editor) }

        respond(
            isCorrect = { addedEditor != null },
            onCorrect = {
                call.respond(
                    status = HttpStatusCode.Created,
                    message = addedEditor!!
                )
            },
            onIncorrect = {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Response(HttpStatusCode.BadRequest.value)
                )
            }
        )
    }
}

private fun Route.deleteEditorById(editorsService: EditorService) {
    delete("/api/v1.0/editors/{id?}") {
        val id = call.parameters["id"] ?: return@delete call.respond(
            status = HttpStatusCode.BadRequest,
            message = Response(HttpStatusCode.BadRequest.value)
        )
        val removedItem = editorsService.delete(id.toLong())

        respond(
            isCorrect = { removedItem },
            onCorrect = {
                call.respond(status = HttpStatusCode.NoContent, Response(HttpStatusCode.OK.value))
            },
            onIncorrect = {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Response(HttpStatusCode.BadRequest.value)
                )
            }
        )
    }
}

private fun Route.getEditorById(editorsService: EditorService) {
    get("/api/v1.0/editors/{id?}") {
        val id = call.parameters["id"] ?: return@get call.respond(
            status = HttpStatusCode.BadRequest,
            message = Response(HttpStatusCode.BadRequest.value)
        )
        val requestedItem = editorsService.getById(id.toLong())

        respond(
            isCorrect = { requestedItem != null },
            onCorrect = {
                call.respond(status = HttpStatusCode.OK, requestedItem!!)
            },
            onIncorrect = {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Response(HttpStatusCode.BadRequest.value)
                )
            }
        )
    }
}

private fun Route.updateEditor(editorsService: EditorService) {
    put("/api/v1.0/editors") {
        val editor: Editor = getWithCheck { call.receive() } ?: return@put call.respond(
            status = HttpStatusCode.BadRequest,
            message = Response(HttpStatusCode.BadRequest.value)
        )
        val mapper = EditorMapper(editor.id)

        val updatedEditor = editorsService.update(
            editorId = editor.id,
            updateEditorDto = getWithCheck { mapper.mapFromEntity(editor) } ?: return@put call.respond(
                status = HttpStatusCode.BadRequest,
                message = Response(HttpStatusCode.BadRequest.value)
            )
        )

        respond(
            isCorrect = { updatedEditor != null },
            onCorrect = {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = updatedEditor!!
                )
            },
            onIncorrect = {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = Response(HttpStatusCode.BadRequest.value)
                )
            }
        )
    }
}