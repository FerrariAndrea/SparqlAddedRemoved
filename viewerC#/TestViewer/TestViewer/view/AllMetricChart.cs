﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using TestViewer.model;

namespace TestViewer.view
{
    public partial class AllMetricChart : UserControl
    {
        private List<MetaTestGroup> groups;
        public AllMetricChart()
        {
            InitializeComponent();
        }

        public void loadMetaTest(List<MetaTestGroup> groups)
        {
            this.groups = groups;
            comboBox1.Items.Clear();
            chart1.Series.Clear();
            //ottengo la lista delle fasi (dando per scontato che sia la medesima per tutti i test)
            if (groups.Count > 0 
                && groups.ElementAt(0).List.Count>0 
                && groups.ElementAt(0).List.ElementAt(0).Tests.Count > 0
            )
            {
                    foreach (Metric m in groups.ElementAt(0).List.ElementAt(0).Tests.ElementAt(0).Phases)
                    {
                        comboBox1.Items.Add(m.Name);
                    }
            }
            if (comboBox1.Items.Count>=0) {
                comboBox1.SelectedIndex = 0;
            }
        }

        public void setMaxY(int max)
        {
            if (max > 0)
            {
                chart1.ChartAreas[0].AxisY.Maximum = max;
            }
            else
            {
                chart1.ChartAreas[0].AxisY.Maximum = double.NaN;
                chart1.ChartAreas[0].RecalculateAxesScale();
            }
        }

        private void AllMetricChart_Load(object sender, EventArgs e)
        {

            chart1.Series.Clear();
        }

        private void comboBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            plot();



        }

        private void radioButton1_CheckedChanged(object sender, EventArgs e)
        {
            plot();

        }
        private void plot() {
            if (comboBox1.SelectedItem != null)
            {
                chart1.Series.Clear();
                foreach (MetaTestGroup group in groups)
                {
                    String name = group.MetaTestName;//.Replace(" ", "_");
                    chart1.Series.Add(name);

                    foreach (MetaTestResult mtr in group.List)
                    {
                        double[] temp = new double[mtr.Tests.Count];
                        int index = 0;
                        double avarage = 0;
                        foreach (TestResult tr in mtr.Tests)
                        {

                            Metric m = tr.getMetricByName((String)comboBox1.SelectedItem);
                            if (m != null)
                            {
                                temp[index] = m.Value;
                                index++;
                                avarage+= m.Value;
                            }//esle ignore
                        }
                        if (radioButton1.Checked)
                        {
                            chart1.Series[name].Points.AddXY(mtr.TripleNumber, temp[index / 2]);
                        }
                        else
                        {
                            chart1.Series[name].Points.AddXY(mtr.TripleNumber, avarage / index);
                        }
                    }
                    chart1.Series[name].ChartType = System.Windows.Forms.DataVisualization.Charting.SeriesChartType.Spline;
                    chart1.Series[name].BorderWidth = 2;
                }

            }
        }
    }
}
