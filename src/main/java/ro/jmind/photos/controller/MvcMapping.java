package ro.jmind.photos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class MvcMapping {


    public static final String JSON_FILENAME = "_result.json";

    @GetMapping("/login")
    public String login() throws IOException, GeneralSecurityException {


        return "done";
    }


}
