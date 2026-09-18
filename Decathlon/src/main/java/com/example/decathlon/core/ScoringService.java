package com.example.decathlon.core;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoringService {
    public enum Type { TRACK, FIELD }
    public enum Discipline { DECATHLON, HEPTATHLON }

    public record EventDef(String id, String label, Discipline discipline, Type type,
                           double A, double B, double C,
                           double minRaw, double maxRaw, String unit) {}

    private final Map<String, EventDef> events = buildEvents();
    private final Map<Discipline, List<String>> order = buildOrder();

    private static Map<String, EventDef> buildEvents() {
        Map<String, EventDef> m = new LinkedHashMap<>();

        m.put("100m", new EventDef("100m", "100m", Discipline.DECATHLON, Type.TRACK,
                25.4347, 18.0, 1.81, 5, 17.8, "s"));
        m.put("longJump", new EventDef("longJump", "Long Jump", Discipline.DECATHLON, Type.FIELD,
                0.14354, 220.0, 1.4, 250, 1000, "cm"));
        m.put("shotPut", new EventDef("shotPut", "Shot Put", Discipline.DECATHLON, Type.FIELD,
                51.39, 1.5, 1.05, 0, 30, "m"));
        m.put("highJump", new EventDef("highJump", "High Jump", Discipline.DECATHLON, Type.FIELD,
                0.8465, 75.0, 1.42, 0, 100, "cm"));
        m.put("400m", new EventDef("400m", "400m", Discipline.DECATHLON, Type.TRACK,
                1.53775, 82.0, 1.81, 20, 100, "s"));
        m.put("110mHurdles", new EventDef("110mHurdles", "110m Hurdles", Discipline.DECATHLON, Type.TRACK,
                5.74352, 28.5, 1.92, 10, 28.5, "s"));
        m.put("discusThrow", new EventDef("discusThrow", "Discus Throw", Discipline.DECATHLON, Type.FIELD,
                12.91, 4.0, 1.1, 0, 85, "m"));
        m.put("poleVault", new EventDef("poleVault", "Pole Vault", Discipline.DECATHLON, Type.FIELD,
                0.2797, 100.0, 1.35, 2, 1000, "cm"));
        m.put("javelinThrow", new EventDef("javelinThrow", "Javelin Throw", Discipline.DECATHLON, Type.FIELD,
                10.14, 7.0, 1.08, 0, 110, "m"));
        m.put("1500m", new EventDef("1500m", "1500m", Discipline.DECATHLON, Type.TRACK,
                0.03768, 480.0, 18.5, 2, 7, "s"));

        m.put("hep100mHurdles", new EventDef("hep100mHurdles", "100m Hurdles", Discipline.HEPTATHLON, Type.TRACK,
                9.23076, 26.7, 18.35, 5, 26.4, "s"));
        m.put("hepHighJump", new EventDef("hepHighJump", "High Jump", Discipline.HEPTATHLON, Type.FIELD,
                1.84523, 75.0, 1.348, 75.7, 270, "cm"));
        m.put("hepShotPut", new EventDef("hepShotPut", "Shot Put", Discipline.HEPTATHLON, Type.FIELD,
                56.0211, 1.5, 1.05, 5, 100, "m"));
        m.put("hep200m", new EventDef("hep200m", "200m", Discipline.HEPTATHLON, Type.TRACK,
                4.99087, 42.5, 1.81, 14, 42.08, "s"));
        m.put("hepLongJump", new EventDef("hepLongJump", "Long Jump", Discipline.HEPTATHLON, Type.FIELD,
                0.1888807, 210.0, 1.41, 0, 400, "m"));
        m.put("hepJavelinThrow", new EventDef("hepJavelinThrow", "Javelin Throw", Discipline.HEPTATHLON, Type.FIELD,
                15.9803, 3.8, 1.04, 0, 100, "m"));
        m.put("hep800m", new EventDef("hep800m", "800m", Discipline.HEPTATHLON, Type.TRACK,
                0.11193, 254.0, 1.88, 70, 250.79, "s"));

        return m;
    }

    private static Map<Discipline, List<String>> buildOrder() {
        Map<Discipline, List<String>> o = new LinkedHashMap<>();
        o.put(Discipline.DECATHLON, List.of(
                "100m", "longJump", "shotPut", "highJump", "400m",
                "110mHurdles", "discusThrow", "poleVault", "javelinThrow", "1500m"));
        o.put(Discipline.HEPTATHLON, List.of(
                "hep100mHurdles", "hepHighJump", "hepShotPut", "hep200m",
                "hepLongJump", "hepJavelinThrow", "hep800m"));
        return o;
    }

    public EventDef get(String id) { return events.get(id); }

    public List<String> eventIds(Discipline discipline) { return order.get(discipline); }

    public List<EventDef> eventDefs(Discipline discipline) {
        return eventIds(discipline).stream().map(events::get).toList();
    }

    public int score(String eventId, double raw) {
        EventDef e = events.get(eventId);
        if (e == null) return 0;
        if (raw < e.minRaw || raw > e.maxRaw) return 0;
        double points;
        if (e.type == Type.TRACK) {
            double x = e.B - raw;
            if (x <= 0) return 0;
            points = e.A * Math.pow(x, e.C);
        } else {
            double x = raw - e.B;
            if (x <= 0) return 0;
            points = e.A * Math.pow(x, e.C);
        }
        return (int) Math.floor(points);
    }
}
