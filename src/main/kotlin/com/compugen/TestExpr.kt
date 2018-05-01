package com.compugen

import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.MapContext
import org.mariuszgromada.math.mxparser.Argument
import java.math.BigDecimal
import java.util.*
import javax.script.ScriptContext
import javax.script.ScriptEngineManager


object TestExpr {

    val expr = "a>10 && b<c+5 && (a+b)<c*4"

    val N = 10
    val e = ScriptEngineManager().getEngineByName("js")
    val mxExpr = org.mariuszgromada.math.mxparser.Expression(expr)
    val evalExpression = com.udojava.evalex.Expression(expr)
    val template = groovy.text.GStringTemplateEngine().createTemplate(expr)

    val jexl = JexlBuilder().create()

    val jexlExp = jexl.createExpression(expr)

    @JvmStatic
    fun main(args: Array<String>) {

        evaluateMany("js", TestExpr::jsEvaluate)
        //   evaluateMany("mxParser", TestExpr::mxParserEvaluate)
        evaluateMany("evalex", TestExpr::evalexEvaluate)
        evaluateMany("groovy", TestExpr::groovyEvaluate)
        evaluateMany("jexl", TestExpr::jexlEvaluate)
    }

    fun evaluate(a: Double, b: Double, c: Double, func: (Double, Double, Double) -> Boolean): Boolean {
        return func(a, b, c)
    }

    fun evaluateMany(name: String, func: (Double, Double, Double) -> Boolean) {
        val results = HashMap<Boolean, Int>()

        val t1 = System.currentTimeMillis()
        for (a in 0..N)
            for (b in 0..N)
                for (c in 0..N) {
                    val r = evaluate(a.toDouble(), b.toDouble(), c.toDouble(), func)
                    results[r] = results.getOrDefault(r, 0) + 1
                }
        val t2 = System.currentTimeMillis()
        println("$name ${(t2 - t1)}, ${results[false]}/${results[true]}")
    }

    fun jsEvaluate(a: Double, b: Double, c: Double): Boolean {
        e.context.setAttribute("a", a, ScriptContext.ENGINE_SCOPE)
        e.context.setAttribute("b", b, ScriptContext.ENGINE_SCOPE)
        e.context.setAttribute("c", c, ScriptContext.ENGINE_SCOPE)
        return e.eval(expr) as Boolean
    }


    fun mxParserEvaluate(a: Double, b: Double, c: Double): Boolean {
        val v1 = Argument("a = $a")
        val v2 = Argument("b = $b")
        val v3 = Argument("c = $c")
        mxExpr.addArguments(v1, v2, v3)
        return mxExpr.calculate() == 1.0
    }

    fun evalexEvaluate(a: Double, b: Double, c: Double): Boolean {
        val eval = evalExpression.with("a", BigDecimal.valueOf(a)).and("b", BigDecimal.valueOf(b)).and("c", BigDecimal.valueOf(c)).eval()
        //  println(eval)
        return eval == BigDecimal.ONE
    }

    fun groovyEvaluate(a: Double, b: Double, c: Double): Boolean {
        val binding = HashMap<String, Double>()
        binding.put("a", a)
        binding.put("b", b)
        binding.put("c", c)

        val template = template.make(binding)
        return template.toString().toBoolean()
    }

    fun jexlEvaluate(a: Double, b: Double, c: Double): Boolean {

        val jc = MapContext()
        jc.set("a", a)
        jc.set("b", b)
        jc.set("c", c)

        return jexlExp.evaluate(jc) as Boolean
    }


}