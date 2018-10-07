package com.example.alva;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

public class TestExcerpt {

    private static final String CONTENT = "<p>Since around 1850, Europe is most commonly considered to be <a "
        + "href=\"/wiki/Borders_of_the_continents#Europe_and_Asia\" class=\"mw-redirect\" title=\"Borders of the "
        + "continents\">separated from Asia</a> by the <a href=\"/wiki/Drainage_divide\" title=\"Drainage "
        + "divide\">watershed divides</a> of the <a href=\"/wiki/Ural_Mountains\" title=\"Ural Mountains\">Ural</a> "
        + "and <a href=\"/wiki/Caucasus_Mountains\" title=\"Caucasus Mountains\">Caucasus Mountains</a>, the <a "
        + "href=\"/wiki/Ural_River\" title=\"Ural River\">Ural River</a>, the <a href=\"/wiki/Caspian_Sea\" "
        + "title=\"Caspian Sea\">Caspian</a> and <a href=\"/wiki/Black_Sea\" title=\"Black Sea\">Black</a> Seas and "
        + "the waterways of the <a href=\"/wiki/Turkish_Straits\" title=\"Turkish Straits\">Turkish Straits</a>.<sup"
        + " id=\"cite_ref-NatlGeoAtlas_7-0\" class=\"reference\"><a href=\"#cite_note-NatlGeoAtlas-7\">[7]</a></sup>"
        + " Although the term \"continent\" implies <a href=\"/wiki/Physical_geography\" title=\"Physical "
        + "geography\">physical geography</a>, the land border is somewhat arbitrary and has moved since its first "
        + "conception in <a href=\"/wiki/Classical_antiquity\" title=\"Classical antiquity\">classical antiquity</a>"
        + ". The division of Eurasia into two continents reflects <a href=\"/wiki/East%E2%80%93West_dichotomy\" "
        + "title=\"East–West dichotomy\">East-West</a> cultural, linguistic and ethnic differences, some of which "
        + "vary on a spectrum rather than with a sharp dividing line. The border does not follow political "
        + "boundaries, with <a href=\"/wiki/Turkey\" title=\"Turkey\">Turkey</a>, <a href=\"/wiki/Russia\" "
        + "title=\"Russia\">Russia</a> and <a href=\"/wiki/Kazakhstan\" title=\"Kazakhstan\">Kazakhstan</a> being <a"
        + " href=\"/wiki/List_of_transcontinental_countries\" title=\"List of transcontinental "
        + "countries\">transcontinental countries</a>.\n" + "</p>";
    private static final ImmutableList<String> CONTAINED_HYPERLINKS = ImmutableList.<String>builder()
        .add("/wiki/Borders_of_the_continents#Europe_and_Asia")
        .add("/wiki/Drainage_divide")
        .add("/wiki/Ural_Mountains")
        .add("/wiki/Caucasus_Mountains")
        .add("/wiki/Ural_River")
        .add("/wiki/Caspian_Sea")
        .add("/wiki/Black_Sea")
        .add("/wiki/Turkish_Straits")
        .add("#cite_note-NatlGeoAtlas-7")
        .add("/wiki/Physical_geography")
        .add("/wiki/Classical_antiquity")
        .add("/wiki/East%E2%80%93West_dichotomy")
        .add("/wiki/Turkey")
        .add("/wiki/Russia")
        .add("/wiki/Kazakhstan")
        .add("/wiki/List_of_transcontinental_countries")
        .build();

    public static URI getBaseUri() {
        return URI.create("https://en.wikipedia.org/wiki/Europe");
    }

    public static Stream<String> streamLines() {
        return Arrays.stream(CONTENT.replaceAll("\n", "").replaceAll("<", "\n<").split("\n"));
    }

    public static List<String> getOriginalHyperlinks() {
        return CONTAINED_HYPERLINKS;
    }

    public static List<String> getInternalAnchors() {
        return ImmutableList.of("#cite_note-NatlGeoAtlas-7");
    }

    public static List<String> getClippedURIs() {
        return ImmutableList.of("/wiki/Borders_of_the_continents");
    }
}
