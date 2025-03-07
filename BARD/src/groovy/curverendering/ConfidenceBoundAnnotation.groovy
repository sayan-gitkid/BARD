/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package curverendering;


import org.hibernate.cfg.NotYetImplementedException
import org.jfree.chart.annotations.AbstractXYAnnotation
import org.jfree.chart.axis.ValueAxis
import org.jfree.chart.plot.Plot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.PlotRenderingInfo
import org.jfree.chart.plot.XYPlot
import org.jfree.ui.RectangleEdge

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.GeneralPath
import java.awt.geom.Rectangle2D

/**
 * Created with IntelliJ IDEA.
 * User: jasiedu
 * Date: 9/19/12
 * Time: 12:29 PM
 * To change this template use File | Settings | File Templates.
 */
class ConfidenceBoundAnnotation extends AbstractXYAnnotation {

    float confidenceX1
    float confidenceX2
    float confidenceY;
    float confidenceAnnotationHeight = 10;
    Color color;

    public ConfidenceBoundAnnotation(float confidenceX1, float confidenceX2, float confidenceY, Color color) {
        this.confidenceX1 = confidenceX1;
        this.confidenceX2 = confidenceX2;
        this.confidenceY = confidenceY;
        this.color = color;
    }

    @Override
    public void draw(Graphics2D gd, XYPlot plot,
                     Rectangle2D dataArea,
                     ValueAxis domainAxis, ValueAxis rangeAxis,
                     int i, PlotRenderingInfo info) {

        PlotOrientation orientation = plot.orientation
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                plot.domainAxisLocation, orientation)

        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                plot.rangeAxisLocation, orientation)
        final Map<String, Float> edges =
            compute2DEdges(orientation, domainEdge, rangeEdge, dataArea, domainAxis, rangeAxis)

        float j2DX1 = edges.j2DX1
        float j2DX2 = edges.j2DX2
        float j2DY1 = edges.j2DY1
        gd.setPaint(this.color)
        gd.setStroke(new BasicStroke())



        GeneralPath bar = new GeneralPath().with {
            moveTo(j2DX1, j2DY1)
            lineTo(j2DX2, j2DY1)
            //the left vertical line
            moveTo(j2DX1, j2DY1 - this.confidenceAnnotationHeight / 2)
            lineTo(j2DX1, j2DY1 + this.confidenceAnnotationHeight / 2)
            // the right vertical line
            moveTo(j2DX2, j2DY1 - this.confidenceAnnotationHeight / 2)
            lineTo(j2DX2, j2DY1 + this.confidenceAnnotationHeight / 2)
            return it
        }
//        GeneralPath bar = new GeneralPath()
//        // the horizontal line
//        bar.moveTo(j2DX1, j2DY1)
//        bar.lineTo(j2DX2, j2DY1)
//        // the left vertical line
//        bar.moveTo(j2DX1, j2DY1 - this.confidenceAnnotationHeight / 2)
//        bar.lineTo(j2DX1, j2DY1 + this.confidenceAnnotationHeight / 2)
//        // the right vertical line
//        bar.moveTo(j2DX2, j2DY1 - this.confidenceAnnotationHeight / 2)
//        bar.lineTo(j2DX2, j2DY1 + this.confidenceAnnotationHeight / 2)

        gd.draw(bar);
    }

    Map<String, Float> compute2DEdges(PlotOrientation orientation, RectangleEdge domainEdge, RectangleEdge rangeEdge, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis) {
        if (orientation == PlotOrientation.VERTICAL) {
            float j2DX1 = (float) domainAxis.valueToJava2D(
                    this.confidenceX1, dataArea, domainEdge)
            float j2DX2 = (float) domainAxis.valueToJava2D(
                    this.confidenceX2, dataArea, domainEdge)
            float j2DY1 = (float) rangeAxis.valueToJava2D(
                    this.confidenceY, dataArea, rangeEdge)
            return [j2DX1: j2DX1, j2DX2: j2DX2, j2DY1: j2DY1]
        }
        throw new NotYetImplementedException("Orientation ${orientation}, Not Yet Implemented")
    }
}
