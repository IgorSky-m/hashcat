package tt.haschat.controller.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tt.haschat.dto.Request;
import tt.haschat.services.api.IRequestService;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class RequestRestController {

    private final IRequestService service;

    public RequestRestController(IRequestService service) {
        this.service = service;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createRequest(@RequestBody Request request) {
        Request savedRequest = this.service.createNewRequest(request);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}").buildAndExpand(savedRequest.getUuid()).toUri());
        return ResponseEntity.status(HttpStatus.ACCEPTED).headers(httpHeaders).build();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Request> getOne(@PathVariable UUID uuid) {
        return ResponseEntity.ok(this.service.getOneById(uuid));
    }

    @GetMapping("/email/{mail}/confirm")
    public ResponseEntity<String> getByMail(@PathVariable UUID mail) {
        this.service.confirmRequest(mail);
        return ResponseEntity.ok("Email confirmed successfully");
    }

}
