package by.bashlikovvv.api.controllers.routings

import by.bashlikovvv.api.dto.request.CreateTweetDto
import by.bashlikovvv.api.dto.request.UpdateTweetDto
import by.bashlikovvv.model.Response
import by.bashlikovvv.services.TweetService
import by.bashlikovvv.util.getWithCheck
import by.bashlikovvv.util.respond
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.tweetsRouting() {
    val tweetService: TweetService by inject()

    getTweets(tweetService)
    createTweet(tweetService)
    deleteTweetById(tweetService)
    getTweetById(tweetService)
    updateTweet(tweetService)
}

private fun Route.getTweets(tweetService: TweetService) {
    get("/api/v1.0/tweets") {
        val tweets = tweetService.getAll()

        respond(
            isCorrect = { tweets.isNotEmpty() },
            onCorrect = { call.respond(status = HttpStatusCode.OK, tweets) },
            onIncorrect = {
                call.respond(status = HttpStatusCode.OK, Response(HttpStatusCode.OK.value))
            }
        )
    }
}

private fun Route.createTweet(tweetsService: TweetService) {
    post("/api/v1.0/tweets") {
        val tweet: CreateTweetDto = call.receive()
        val addedTweet = getWithCheck { tweetsService.create(tweet) }

        respond(
            isCorrect = { addedTweet != null },
            onCorrect = {
                call.respond(
                    status = HttpStatusCode.Created,
                    message = addedTweet!!
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

private fun Route.deleteTweetById(tweetsService: TweetService) {
    delete("/api/v1.0/tweets/{id?}") {
        val id = call.parameters["id"] ?: return@delete call.respond(
            status = HttpStatusCode.BadRequest,
            message = Response(HttpStatusCode.BadRequest.value)
        )
        val removedItem = tweetsService.delete(id.toLong())

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

private fun Route.getTweetById(tweetsService: TweetService) {
    get("/api/v1.0/tweets/{id?}") {
        val id = call.parameters["id"] ?: return@get call.respond(
            status = HttpStatusCode.BadRequest,
            message = Response(HttpStatusCode.BadRequest.value)
        )
        val requestedItem = tweetsService.getById(id.toLong())

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

private fun Route.updateTweet(tweetsService: TweetService) {
    put("/api/v1.0/tweets") {
        val updateTweetDto: UpdateTweetDto = getWithCheck { call.receive() } ?: return@put call.respond(
            status = HttpStatusCode.BadRequest,
            message = Response(HttpStatusCode.BadRequest.value)
        )
        val updatedTweet = tweetsService.update(
            tweetId = updateTweetDto.editorId,
            updateTweetDto = UpdateTweetDto(
                editorId = updateTweetDto.editorId,
                title = updateTweetDto.title,
                content = updateTweetDto.content,
                name = updateTweetDto.name
            )
        )

        respond(
            isCorrect = { updatedTweet != null },
            onCorrect = {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = updatedTweet!!
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