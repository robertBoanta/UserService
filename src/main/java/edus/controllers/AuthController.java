package edus.controllers;

import edus.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @GetMapping("/userData")
    public Map<String,Object> getUserData(@AuthenticationPrincipal Jwt jwt){
       return authService.getUserData(jwt);
    }

    @PutMapping("/updateAdresses")
    public ResponseEntity<String> updateUserAdresses(@AuthenticationPrincipal Jwt jwt,
                                   @RequestBody Map<String,Object> body){
        try {
            String userId = jwt.getSubject();
            String institutieInvatamant = (String) body.get("institutieInvatamant");
            String numePrenume = (String) body.get("numePrenume");
            var adreseLivrare = (List<Object>) body.get("adreseLivrare");
            Object adresaFacturare = body.get("adresaFacturare");

            authService.updateUserAdresses(userId, institutieInvatamant, numePrenume, adreseLivrare, adresaFacturare);
            return ResponseEntity.ok("Adrese actualizate cu succes.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la actualizarea adreselor: " + e.getMessage());
        }
    }
}
