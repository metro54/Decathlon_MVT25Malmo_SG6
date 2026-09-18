package com.example.decathlon.api;

import com.example.decathlon.core.CompetitionService;
import com.example.decathlon.core.ScoringService;
import com.example.decathlon.dto.ScoreReq;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final CompetitionService comp;
    private final ScoringService scoring;

    public ApiController(CompetitionService comp, ScoringService scoring) {
        this.comp = comp;
        this.scoring = scoring;
    }

    @PostMapping("/competitors")
    public ResponseEntity<?> add(@RequestBody Map<String,String> body) {
        String name = Optional.ofNullable(body.get("name")).orElse("");

        if (getCount() >= 40) {
            return ResponseEntity.status(429).body("Too many competitors");
        }

        try {
            comp.addCompetitor(name);
        } catch (CompetitionService.InvalidNameException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(201).build();
    }

    private int getCount() {
        return comp.standings().size();
    }

    @PostMapping("/score")
    public ResponseEntity<?> score(@RequestBody ScoreReq r) {
        try {
            int pts = comp.score(r.name(), r.event(), r.raw());
            return ResponseEntity.ok(Map.of("points", pts));
        } catch (CompetitionService.CompetitorNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/standings")
    public List<Map<String,Object>> standings() { return comp.standings(); }

    @GetMapping("/events")
    public List<Map<String,Object>> events(@RequestParam(value = "discipline", defaultValue = "decathlon") String discipline) {
        ScoringService.Discipline d = "heptathlon".equalsIgnoreCase(discipline)
                ? ScoringService.Discipline.HEPTATHLON
                : ScoringService.Discipline.DECATHLON;
        return scoring.eventDefs(d).stream()
                .map(e -> {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("id", e.id());
                    m.put("label", e.label());
                    m.put("unit", e.unit());
                    return (Map<String,Object>) m;
                })
                .toList();
    }

    @GetMapping(value="/export.csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public String export() { return comp.exportCsv(); }
}
