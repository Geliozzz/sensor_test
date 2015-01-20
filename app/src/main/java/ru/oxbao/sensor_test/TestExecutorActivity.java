package ru.oxbao.sensor_test;


import android.content.Intent;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TestExecutorActivity extends ActionBarActivity
{
    // GUI
    private RadioGroup m_radioGroupTests;
    private Button m_buttonStartTest;
    private Button m_buttonBack;
    private Spinner m_spinnerFiles;
    private ProgressWheel m_progressBarWheel;
    // Service
    private boolean m_popupIsStarted = false;
    public static boolean g_startTestFlag = false;
    private boolean m_isHomeButton = true;
    final String LOG_TAG = "TestExecutorActivity";
    // Objects
    private Handler m_handler;
    private TestExecutor m_testExecutor;
    // Variables
    private TestResult m_testResult;
    private String m_carName;
    private InputDataActivity.EngineTypeEnum m_carEngineType;

    // Temporary
    private RadioButton m_radioTest1;
    private TextView m_textView1;
    private TextView m_textView2;
    private TextView m_textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executor_test);
        Log.d(LOG_TAG, "CREATE");

        m_carName = getIntent().getStringExtra("CarName");
        m_carEngineType = InputDataActivity.ToEngineTypeEnum(getIntent().getIntExtra("CarEngineType",
                InputDataActivity.EngineTypeEnum.Petrol.ordinal()));

        m_buttonStartTest = (Button) findViewById(R.id.btnStartTest);
        m_buttonBack = (Button) findViewById(R.id.btnBack);
        m_progressBarWheel = (ProgressWheel) findViewById(R.id.progressBarWheel);

        m_radioGroupTests = (RadioGroup) findViewById(R.id.radGrTests);
        m_textView1 = (TextView) findViewById(R.id.tvX);
        m_textView2 = (TextView) findViewById(R.id.tvY);
        m_textView3 = (TextView) findViewById(R.id.tvZ);
        m_spinnerFiles = (Spinner) findViewById(R.id.spinnerFiles);
        m_radioTest1 = (RadioButton) findViewById(R.id.test1);

        m_testResult = new TestResult();
        m_testExecutor = new TestExecutor(this, m_testResult);

        getSupportActionBar().hide();
        InputDataActivity.g_flagEraseData = true; // Стереть введенную информацию. Снимет этот флаг кнопка назад или системнаяя назад

        /***********Запрет на зысыпание*****************/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                m_progressBarWheel.setProgress(msg.getData().getInt("count"));
            }
        };

        m_buttonStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartTest();
            }
        });

        m_buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputDataActivity.g_flagEraseData = false;
                m_isHomeButton = false;
                m_testExecutor.Stop();
                finish();
            }
        });

        m_radioGroupTests.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.test1:
                        m_spinnerFiles.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.test2:
                        if (m_testExecutor.GetFilesNames() != null) {
                            m_spinnerFiles.setVisibility(View.VISIBLE);
                            m_spinnerFiles.setAdapter(m_testExecutor.GetFilesNames());
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.filesNotFound), Toast.LENGTH_SHORT).show();
                            m_radioTest1.setChecked(true);
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "Resume");
        SetProgressBar(0);
        /***Ready for test*/
        if (m_popupIsStarted) {
            SetButtonEnabled(true);
            m_popupIsStarted = false;
        }
        if (g_startTestFlag){
            StartTest();
            g_startTestFlag = false;
        }
        m_isHomeButton = true;
        super.onResume();
    }


    public void SetProgressBar(int progress) {
        m_progressBarWheel.setProgress(progress);
    }

    public void SetMaxProgressBar(int max) {

        m_progressBarWheel.setBarLength(max);
    }

    public void OnTestFinished(Solutions.ResultTestEnum resultTestEnum)
    {
        String stringToShow = "Common result: " + Solutions.ToString(resultTestEnum) +
                ". " + m_testResult.ResultString;

        ShowResult(stringToShow);
    }

    public void SetTextViews(double x, double y, double z) {
        m_textView1.setText(String.valueOf(x));
        m_textView2.setText(String.valueOf(y));
        m_textView3.setText(String.valueOf(z));
    }

    public String GetCheckedSpinner() {
        String result = "";
        try {
            result = m_spinnerFiles.getSelectedItem().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void SendMessage(int count) {
        Bundle bundle = new Bundle();
        bundle.putInt("count", count);
        Message message = new Message();
        message.setData(bundle);
        m_handler.sendMessage(message);
    }

    // При чтении с датчика возникают события новых значений после остановки.
    // Иногда открывается несколько результатов
    //Что б не открвывалось больше одного результата введен флаг.
    private void ShowResult(String resultString)
    {
        if (!m_popupIsStarted)
        {
            Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
            intent.putExtra("TestDataValue", resultString);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            m_isHomeButton = false;
            m_popupIsStarted = true;
            startActivity(intent);
        }
    }

    private void SetButtonEnabled(boolean flag) {
        if (flag) {
            m_buttonStartTest.setBackgroundResource(R.drawable.oval_button_ready);
            m_buttonStartTest.setText(getResources().getString(R.string.startTest));
            m_buttonStartTest.setEnabled(true);
            m_buttonStartTest.setTextColor(getResources().getColor(R.color.btnStartReadyTextColor));
        } else {
            m_buttonStartTest.setText(getResources().getString(R.string.wait));
            m_buttonStartTest.setBackgroundResource(R.drawable.oval_button_execute);
            m_buttonStartTest.setTextColor(getResources().getColor(R.color.btnExecuteTextColor));
            m_buttonStartTest.setEnabled(false);
        }
    }

    public void StartTest()
    {
        switch (m_radioGroupTests.getCheckedRadioButtonId()) {
            case R.id.test1:
                m_testExecutor.Start(TestExecutor.TestEnum.test1);
                break;
            case R.id.test2:
                m_testExecutor.Start(TestExecutor.TestEnum.test2);
                break;
        }

        SetButtonEnabled(false);
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(LOG_TAG, "FINALIZE");
        super.finalize();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            Log.d(LOG_TAG, "Back");
            InputDataActivity.g_flagEraseData = false;
            m_isHomeButton = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "PAUSE");
        m_testExecutor.Stop();
        if (m_isHomeButton){
            InputDataActivity.g_flagEraseData = true;
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "STOP");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "Destroy");
        super.onDestroy();
    }
}
