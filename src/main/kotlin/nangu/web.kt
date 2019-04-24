package nangu

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus.*
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/messages")
class MessageController(val messageRepository: MessageRepository) {

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): Message =
        messageRepository.findByIdOrNull(id) ?: throw NotFoundException()

    @GetMapping
    fun findAll(
        @RequestParam(defaultValue = "") text: String,
        @RequestParam(required = false) author: String?
    ): List<Message> {
        return messageRepository.findAllByTextAndAuthor(text, author)
    }

    @PostMapping
    fun create(@RequestBody params: MessageParams, authentication: Authentication): Message {
        return messageRepository.save(Message(
            text = params.text,
            author = authentication.name
        ))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody params: MessageParams,
        authentication: Authentication
    ): Message {
        val old = messageRepository.findByIdOrNull(id) ?: throw NotFoundException()
        if (old.author != authentication.name) {
            throw ForbiddenException()
        }

        return messageRepository.save(Message(
            id = old.id,
            text = params.text,
            author = old.author
        ))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, authentication: Authentication) {
        val message = messageRepository.findByIdOrNull(id) ?: throw NotFoundException()
        if (message.author != authentication.name) {
            throw ForbiddenException()
        }

        messageRepository.delete(message)
    }
}

data class MessageParams(val text: String)

@ResponseStatus(NOT_FOUND)
class NotFoundException : Exception()

@ResponseStatus(FORBIDDEN)
class ForbiddenException : Exception()
