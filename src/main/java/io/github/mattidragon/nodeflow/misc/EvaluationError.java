package io.github.mattidragon.nodeflow.misc;

import net.minecraft.text.Text;

import java.util.Locale;

public record EvaluationError(Type type, Object... data) {
    /**
     * Contains all the types of errors a graph evaluation can result in. May be extended in the future, <b>please add a default case to any switches</b>
     */
    public enum Type {
        /**
         * A node is not fully connected
         */
        NOT_CONNECTED(false),
        /**
         * A node has an invalid config
         */
        INVALID_CONFIG(false),
        /**
         * A connection has incompatible type at both ends.
         */
        MISMATCHED_CONNECTION_TYPES(true),
        /**
         * A node failed to evaluate.
         */
        EVALUATION_ERROR(true),
        /**
         * A node returned the wrong number of outputs
         */
        UNEXPECTED_OUTPUT_COUNT(true),
        /**
         * A node doesn't have the necessary context to execute
         */
        MISSING_CONTEXTS(true),
        /**
         * A node returned the wrong type of output
         */
        UNEXPECTED_OUTPUT_TYPE(true),
        /**
         * All the inputs for a node couldn't be calculated without the node. Usually caused by a (illegal) recursive graph.
         */
        UNRESOLVABLE_NODES(true);

        /**
         * If true the problem wasn't caused by a user, but instead a broken node or unobtainable network.
         */
        public final boolean isInternal;

        Type(boolean isInternal) {
            this.isInternal = isInternal;
        }

        public EvaluationError error(Object... data) {
            return new EvaluationError(this, data);
        }
    }
    /**
     * Returns a text instance with an explanation for the user.
     */
    public Text getName() {
        return Text.translatable("nodeflow.graph.error." + type.name().toLowerCase(Locale.ROOT), data);
    }
}
