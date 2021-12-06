package cz.raixo.blocks.effects.executor;

public interface ExecutionToken {

    void executeNow();

    void executeAfter(long millis);

    boolean shouldStop();

    abstract class Simple implements ExecutionToken {

        private final ParticleExecutor particleExecutor;

        public Simple(ParticleExecutor particleExecutor) {
            this.particleExecutor = particleExecutor;
        }

        @Override
        public boolean shouldStop() {
            return particleExecutor.isDisabled();
        }

    }

}
