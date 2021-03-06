package co.icecave.dialekt.renderer;

import co.icecave.dialekt.ast.EmptyExpression;
import co.icecave.dialekt.ast.ExpressionInterface;
import co.icecave.dialekt.ast.LogicalAnd;
import co.icecave.dialekt.ast.LogicalNot;
import co.icecave.dialekt.ast.LogicalOr;
import co.icecave.dialekt.ast.Pattern;
import co.icecave.dialekt.ast.PatternChildInterface;
import co.icecave.dialekt.ast.PatternLiteral;
import co.icecave.dialekt.ast.PatternWildcard;
import co.icecave.dialekt.ast.Tag;
import co.icecave.dialekt.ast.VisitorInterface;
import co.icecave.dialekt.renderer.exception.RenderException;
import java.util.List;

/**
 * Interface for node visitors.
 */
public class ExpressionRenderer implements RendererInterface, VisitorInterface<String>
{
    public ExpressionRenderer()
    {
        this.wildcardString = "*"; // Token::WILDCARD_CHARACTER;
    }

    /**
     * @param wildcardString The string to use as a wildcard placeholder.
     */
    public ExpressionRenderer(String wildcardString)
    {
        this.wildcardString = wildcardString;
    }

    /**
     * Render an expression to a string.
     *
     * @param expression The expression to render.
     *
     * @return The rendered expression.
     */
    public String render(ExpressionInterface expression)
    {
        return expression.accept(this);
    }

    /**
     * Visit a LogicalAnd node.
     *
     * @internal
     *
     * @param node The node to visit.
     */
    public String visit(LogicalAnd node)
    {
        return this.implodeNodes("AND", node.children());
    }

    /**
     * Visit a LogicalOr node.
     *
     * @internal
     *
     * @param node The node to visit.
     */
    public String visit(LogicalOr node)
    {
        return this.implodeNodes("OR", node.children());
    }

    /**
     * Visit a LogicalNot node.
     *
     * @internal
     *
     * @param node The node to visit.
     */
    public String visit(LogicalNot node)
    {
        return "NOT " + node.child().accept(this);
    }

    /**
     * Visit a Tag node.
     *
     * @internal
     *
     * @param node The node to visit.
     */
    public String visit(Tag node)
    {
        return this.escapeString(node.name());
    }

    /**
     * Visit a pattern node.
     *
     * @internal
     *
     * @param node The node to visit.
     */
    public String visit(Pattern node)
    {
        String string = "";

        for (PatternChildInterface n : node.children()) {
            string += n.accept(this);
        }

        return this.escapeString(string);
    }

    /**
     * Visit a PatternLiteral node.
     *
     * @internal
     *
     * @param node The node to visit.
     *
     * @throws RenderException if the literal string contains the wildcard character.
     */
    public String visit(PatternLiteral node)
    {
        if (node.string().contains(this.wildcardString)) {
            throw new RenderException(
                String.format(
                    "The pattern literal \"%s\" contains the wildcard string \"%s\".",
                    node.string(),
                    this.wildcardString
                )
            );
        }

        return node.string();
    }

    /**
     * Visit a PatternWildcard node.
     *
     * @internal
     *
     * @param node The node to visit.
     */
    public String visit(PatternWildcard node)
    {
        return this.wildcardString;
    }

    /**
     * Visit a EmptyExpression node.
     *
     * @internal
     *
     * @param node The node to visit.
     */
    public String visit(EmptyExpression node)
    {
        return "NOT " + this.wildcardString;
    }

    private String implodeNodes(String separator, List<ExpressionInterface> nodes)
    {
        String result = "";

        for (ExpressionInterface node : nodes) {
            if (!result.equals("")) {
                result += " " + separator + " ";
            }

            result += node.accept(this);
        }

        return "(" + result + ")";
    }

    private String escapeString(String string)
    {
        if (
            "and".equalsIgnoreCase(string)
            || "or".equalsIgnoreCase(string)
            || "not".equalsIgnoreCase(string)
        ) {
            return '"' + string + '"';
        }

        String[] characters = { "\\", "(", ")", "\"" };

        for (String s : characters) {
            string = string.replace(s, '\\' + s);
        }

        if (string.matches(".*[\\s\\\\].*")) {
            return '"' + string + '"';
        }

        return string;
    }

    private String wildcardString;
}
