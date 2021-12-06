package cz.raixo.blocks.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Cooldown {

    private final TimeUnit timeUnit;
    private final long cooldown;
    private Date blockedUntil = new Date(0);

    public Cooldown(TimeUnit timeUnit, long cooldown) {
        this.timeUnit = timeUnit;
        this.cooldown = cooldown;
    }

    public boolean canUse() {
        return !(blockedUntil.getTime() - System.currentTimeMillis() > 0);
    }

    public void use() {
        this.blockedUntil = new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(this.cooldown, this.timeUnit));
    }

}
