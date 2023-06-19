package com.williambl.dfunc.impl.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import static org.codehaus.groovy.syntax.Types.*;

@GroovyASTTransformation
public final class ArithmeticCustomizer extends AbstractASTTransformation {
    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        this.init(nodes, source);
        ((ClassNode) nodes[1]).getMethods().forEach(methodNode -> {
            if (methodNode.getCode() instanceof BlockStatement statement) {
                statement.getStatements().replaceAll(stmt -> {
                    if (stmt instanceof ExpressionStatement expr) {
                        return GeneralUtils.stmt(transformExpression(expr.getExpression()));
                    }
                    return stmt;
                });
            } else {
                if (methodNode.getCode() instanceof ExpressionStatement expr) {
                    methodNode.setCode(GeneralUtils.stmt(transformExpression(expr.getExpression())));
                }
            }
        });
    }

    private Expression transformExpression(Expression expr) {
        if (expr instanceof MethodCallExpression ex) {
            ((TupleExpression) ex.getArguments()).getExpressions().replaceAll(this::transformExpression);
        } else if (expr instanceof StaticMethodCallExpression ex) {
            ((TupleExpression) ex.getArguments()).getExpressions().replaceAll(this::transformExpression);
        } else {
            return transformSubExpression(expr);
        }
        return expr;
    }

    private Expression transformSubExpression(Expression expr) {
        if (expr instanceof BinaryExpression bin && bin.getOperation().getType() == 123) {
            return switch (bin.getOperation().getType()) {
                case COMPARE_EQUAL -> GeneralUtils.callX(
                        GeneralUtils.varX("compiler"),
                        "redirectEquals",
                        GeneralUtils.args(bin.getLeftExpression(), bin.getRightExpression())
                );

                case COMPARE_LESS_THAN -> GeneralUtils.callX(
                        GeneralUtils.varX("compiler"),
                        "redirectLessThan",
                        GeneralUtils.args(bin.getLeftExpression(), bin.getRightExpression(), GeneralUtils.constX(false))
                );
                case COMPARE_GREATER_THAN -> GeneralUtils.callX(
                        GeneralUtils.varX("compiler"),
                        "redirectGreaterThan",
                        GeneralUtils.args(bin.getLeftExpression(), bin.getRightExpression(), GeneralUtils.constX(false))
                );
                case COMPARE_LESS_THAN_EQUAL -> GeneralUtils.callX(
                        GeneralUtils.varX("compiler"),
                        "redirectLessThan",
                        GeneralUtils.args(bin.getLeftExpression(), bin.getRightExpression(), GeneralUtils.constX(true))
                );
                case COMPARE_GREATER_THAN_EQUAL -> GeneralUtils.callX(
                        GeneralUtils.varX("compiler"),
                        "redirectGreaterThan",
                        GeneralUtils.args(bin.getLeftExpression(), bin.getRightExpression(), GeneralUtils.constX(true))
                );
                default -> bin;
            };
        }
        return expr;
    }
}
