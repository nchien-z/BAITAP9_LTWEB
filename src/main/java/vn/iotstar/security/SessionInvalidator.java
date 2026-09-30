package vn.iotstar.security;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
@Component
public class SessionInvalidator {
    private final SessionRegistry sessions;
    public SessionInvalidator(SessionRegistry sessions) { this.sessions=sessions; }
    public void expire(Long userId) {
        for (Object principal : sessions.getAllPrincipals())
            if (principal instanceof CustomUserDetails user && user.getId().equals(userId))
                sessions.getAllSessions(principal, false).forEach(s -> s.expireNow());
    }
}
