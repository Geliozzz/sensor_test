package ru.oxbao.speed_car;


import android.widget.ArrayAdapter;
import android.widget.Toast;

public class TestExecutor
{
    public enum ToastMessage
    {
        failSaveData,
        failStartSensor
    }

    public enum TestEnum
    {
        test1,
        test2
    }

    // Flags, parameters
    private boolean m_saveData = true;
    private int m_numberOfMeasurements;
    private String m_prefix = "UnKnown";
    // Objects
    private Collector m_collector;
    private Saver m_saver;
    public TestData g_testData;
    private WorkMath m_workMath;
    private ActivityTestExecutor m_ownerActivity;
    // Output
    public TestResult g_testResult;

    public TestExecutor(ActivityTestExecutor activityTestExecutor, TestResult testResult)
    {
        m_workMath = new WorkMath();
        m_numberOfMeasurements = WorkMath.NumberOfMeasurements;
        m_ownerActivity = activityTestExecutor;
        m_ownerActivity.SetMaxProgressBar(m_numberOfMeasurements);
        g_testResult = testResult;

        g_testData = new TestData(m_numberOfMeasurements);
        m_collector = new Collector(activityTestExecutor, this, m_numberOfMeasurements);
        m_saver = new Saver(this, m_prefix);
    }

    public void Start(TestEnum testEnum)
    {
        if (testEnum.equals(TestEnum.test1))
        {
            m_collector.Start(InputInterface.InputTypeEnum.sensors);
            m_saveData = true;
        } else if (testEnum.equals(TestEnum.test2))
        {
            m_collector.Start(InputInterface.InputTypeEnum.storage);
            m_saveData = false;
        }
        m_saver.SetPrefix(testEnum.toString());

    }

    public void Stop()
    {
        m_collector.Stop();
    }


    public void OnDataCollected()
    {
        if (m_saveData)
        {
            m_saver.SaveData(g_testData, false);
        } else
        {
            m_saver.SaveData(g_testData, true); // for alternative
        }
        m_ownerActivity.OnTestFinished(g_testResult.ResultEnum);
    }

    public void ShowToast(ToastMessage toastMessage)
    {
        if (toastMessage.equals(ToastMessage.failSaveData))
        {
            Toast toast = Toast.makeText(m_ownerActivity.getApplicationContext(), m_ownerActivity.getApplicationContext().getResources().getString(R.string.failSaveFile), Toast.LENGTH_SHORT);
            toast.show();
        }
        if (toastMessage.equals(ToastMessage.failStartSensor))
        {
            Toast toast = Toast.makeText(m_ownerActivity.getApplicationContext(), m_ownerActivity.getApplicationContext().getResources().getString(R.string.failAccelerometer), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public ArrayAdapter GetFilesNames()
    {
        try
        {
            String[] files = m_collector.GetFilesNames();
            if (files.length == 0)
            {
                return null;
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(m_ownerActivity, R.layout.support_simple_spinner_dropdown_item, files);
            return arrayAdapter;
        } catch (NullPointerException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String GetCheckedSpinner()
    {
        return m_ownerActivity.GetCheckedSpinner();
    }

    public void SetFixedTestData()
    {
        g_testData = new TestData(m_numberOfMeasurements);
    }

    public int Getm_numberOfMeasurements()
    {
        return m_numberOfMeasurements;
    }
}
