package ru.hse.multitracker.ui.elements

import android.content.Context
import androidx.core.content.ContextCompat
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import ru.hse.multitracker.R
import ru.hse.multitracker.data.database.entities.TestSession

class ResultsGraph(private val graphview: GraphView, private val context: Context) {
    private val accuracySeries = LineGraphSeries<DataPoint>()
    private val reactionSeries = LineGraphSeries<DataPoint>()

    init {
        // set up accuracy series
        accuracySeries.color = ContextCompat.getColor(context, R.color.green)
        accuracySeries.title = ContextCompat.getString(context, R.string.accuracy)
        accuracySeries.isDrawDataPoints = true
        accuracySeries.isDrawBackground = true
        accuracySeries.backgroundColor =
            ContextCompat.getColor(context, R.color.green_semitransparent)
        graphview.addSeries(accuracySeries)

        // set up reaction time series
        reactionSeries.color = ContextCompat.getColor(context, R.color.red)
        reactionSeries.title = ContextCompat.getString(context, R.string.reaction_time)
        reactionSeries.isDrawDataPoints = true
        reactionSeries.isDrawBackground = true
        reactionSeries.backgroundColor =
            ContextCompat.getColor(context, R.color.red_semitransparent)
        graphview.secondScale.addSeries(reactionSeries)

        // set label formatters
        graphview.gridLabelRenderer.setLabelFormatter(object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    // show dates for x values
                    val dateFormat = android.text.format.DateFormat.getDateFormat(context)
                    dateFormat.format(value.toLong())
                } else {
                    // show percent for accuracy values
                    super.formatLabel(value, isValueX) + "%"
                }
            }
        })
        graphview.secondScale.setLabelFormatter(object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    // not change x labels
                    super.formatLabel(value, isValueX)
                } else {
                    // show seconds for reaction time values
                    val second = ContextCompat.getString(context, R.string.second)
                    " ${super.formatLabel(value, isValueX)} $second"
                }
            }
        })

        // activate horizontal zooming and scaling
        graphview.viewport.isScalable = true

        graphview.gridLabelRenderer.setHorizontalLabelsAngle(20)

        // display legend
        graphview.legendRenderer.isVisible = true
        graphview.legendRenderer.align = LegendRenderer.LegendAlign.TOP

        // make bounds manual
        graphview.viewport.isXAxisBoundsManual = true
        graphview.viewport.isYAxisBoundsManual = true

        // set labels' text size
        graphview.gridLabelRenderer.setTextSize(
            context.resources.getDimension(R.dimen.graph_text_size)
        )
        graphview.gridLabelRenderer.reloadStyles()

        // set legend text size
        graphview.legendRenderer.setTextSize(
            context.resources.getDimension(R.dimen.graph_text_size)
        )
    }

    fun updateData(sessions: List<TestSession>) {
        accuracySeries.resetData(
            sessions.map { session ->
                DataPoint(session.date, session.accuracy * 100)
            }.toTypedArray()
        )

        reactionSeries.resetData(
            sessions.map { session ->
                DataPoint(
                    session.date,
                    session.reactionTime.toDouble()
                )
            }.toTypedArray()
        )

        // set manual y bounds
        graphview.getSecondScale().setMinY(0.0)
        graphview.getSecondScale().setMaxY(sessions.maxOf { it.reactionTime }.toDouble())
        graphview.viewport.setMinY(0.0)
        graphview.viewport.setMaxY(100.0)

        // set manual x bounds
        graphview.viewport.setMinX(sessions.first().date.time.toDouble())
        graphview.viewport.setMaxX(sessions.last().date.time.toDouble())
    }
}