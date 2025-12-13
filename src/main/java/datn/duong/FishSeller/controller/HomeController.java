package datn.duong.FishSeller.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthCheck")
public class HomeController {
    @GetMapping
    public String healthChecked() {
        return "Application is running";
    }
}
