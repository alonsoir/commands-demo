package ch.maxant.commands.demo.framework.commands;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "T_COMMAND")
@NamedQueries({
        @NamedQuery(name = Command.NQSelectAllAvailable.NAME, query = Command.NQSelectAllAvailable.QUERY),
        @NamedQuery(name = Command.NQSelectLocked.NAME, query = Command.NQSelectLocked.QUERY)
})
public class Command {

    public static class NQSelectAllAvailable {
        public static final String NAME = "Command.selectAll";
        public static final String QUERY = "select c from Command c where c.attempts < " +
                CommandService.MAX_NUM_RETRIES + " and c.locked is null order by c.id";
    }

    public static class NQSelectLocked {
        public static final String NAME = "Command.selectLocked";
        public static final String QUERY = "select c from Command c where c.locked is not null and c.locked < ?1";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "COMMAND", nullable = false, updatable = false)
    private String command;

    @Column(name = "ATTEMPTS", nullable = false)
    private int attempts = 0;

    @Column(name = "LOCKED")
    private LocalDateTime locked = LocalDateTime.now();

    /**
     * an ID which is generated by the client which the service provider we are going to
     * call can use to implement idempotency. basically, if they get an ID theyve already seen
     * they have to answer the same as they did when they first saw it.
     */
    @Column(name = "IDEMPOTENCY_ID", nullable = false, updatable = false, length = 36)
    private String idempotencyId = UUID.randomUUID().toString();

    /**
     * contains input parameters. serialised JSON. could include say a version number, so
     * current software can deal with both old and new entries.
     */
    @Column(name = "CONTEXT", nullable = false, updatable = false)
    private String context;

    public Command() {
    }

    public Command(Class command, String context) {
        this.command = command.getCanonicalName();
        this.context = context;
    }

    public long getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public int getAttempts() {
        return attempts;
    }

    public LocalDateTime getLocked() {
        return locked;
    }

    public String getIdempotencyId() {
        return idempotencyId;
    }

    public void resetLocked() {
        this.locked = null;
    }

    public void lock() {
        this.locked = LocalDateTime.now();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public String getContext() {
        return context;
    }

}