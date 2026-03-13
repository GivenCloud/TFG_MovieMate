package com.moviemate.controller;

import com.moviemate.dto.CommentResponse;
import com.moviemate.dto.RatingResponse;
import com.moviemate.dto.ReportResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.ContentReport;
import com.moviemate.entity.Role;
import com.moviemate.entity.User;
import com.moviemate.repository.UserRepository;
import com.moviemate.service.CommentService;
import com.moviemate.service.ContentReportService;
import com.moviemate.service.RatingService;
import com.moviemate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RatingService ratingService;
    private final CommentService commentService;
    private final ContentReportService reportService;

    // ── Usuarios ─────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers(
            @RequestParam(required = false) String q) {
        List<User> users = (q != null && !q.isBlank())
                ? userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q)
                : userRepository.findAll();
        return ResponseEntity.ok(users.stream()
                .map(userService::mapToUserResponse)
                .toList());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userService.findUserById(id);
        user.setRole(Role.valueOf(body.get("role")));
        return ResponseEntity.ok(userService.mapToUserResponse(userRepository.save(user)));
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<UserResponse> banUser(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        User user = userService.findUserById(id);
        user.setBanned(body.getOrDefault("banned", false));
        return ResponseEntity.ok(userService.mapToUserResponse(userRepository.save(user)));
    }

    // ── Contenido ────────────────────────────────────────────

    @GetMapping("/ratings/{id}")
    public ResponseEntity<RatingResponse> getRating(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.getRatingById(id));
    }

    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.adminDeleteRating(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Reportes ─────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(required = false) ContentReport.ReportStatus status) {
        return ResponseEntity.ok(reportService.getReports(status));
    }

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.resolveReport(id));
    }

    @PutMapping("/reports/{id}/dismiss")
    public ResponseEntity<ReportResponse> dismissReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.dismissReport(id));
    }
}
