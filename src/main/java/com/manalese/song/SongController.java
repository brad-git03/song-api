package com.manalese.song;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Controller
@ResponseBody
@RequestMapping("/manalese/songs")
public class SongController {

    private final SongRepository songRepository;

    public SongController(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    // POST /manalese/songs -> add a new song
    @PostMapping
    public ResponseEntity<Song> createSong(@RequestBody Song song) throws URISyntaxException {
        Song savedSong = songRepository.save(song);

        // Postman expects 200; returning 200 OK while still providing a Location is fine.
        return ResponseEntity.ok()
                .location(new URI("/manalese/songs/" + savedSong.getId()))
                .body(savedSong);
    }

    // GET /manalese/songs -> retrieve all songs
    @GetMapping
    public Iterable<Song> getAllSongs() {
        return songRepository.findAll();
    }

    // PUT /manalese/songs/{id} -> update song by id
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateSong(@PathVariable Long id, @RequestBody Song updated) {
        return songRepository.findById(id)
                .<ResponseEntity<Object>>map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setArtist(updated.getArtist());
                    existing.setAlbum(updated.getAlbum());
                    existing.setGenre(updated.getGenre());
                    existing.setUrl(updated.getUrl());

                    Song saved = songRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                // Postman collection doesn’t specify not-found behavior; this is sensible.
                .orElseGet(() -> ResponseEntity.status(404).body("Song with ID " + id + " not found."));
    }

    // GET /manalese/songs/{id} -> retrieve song by id
    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Long id) {
        Optional<Song> song = songRepository.findById(id);
        return song.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /manalese/songs/{id} -> delete song by id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSong(@PathVariable Long id) {
        if (!songRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        songRepository.deleteById(id);

        // Must match Postman exactly: "Song with ID 1 deleted."
        return ResponseEntity.ok("Song with ID " + id + " deleted.");
    }

    // GET /manalese/songs/search/{keyword} -> search songs
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<Song>> searchSongs(@PathVariable String keyword) {
        List<Song> results =
                songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCaseOrGenreContainingIgnoreCase(
                        keyword, keyword, keyword, keyword
                );
        return ResponseEntity.ok(results);
    }
}