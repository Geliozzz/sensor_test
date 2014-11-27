package ru.oxbao.sensor_test;


public class Collector {
    private InputInterface m_inputInterface;
<<<<<<< HEAD
    private int m_numberOfMeasurements;
    private int m_count;
    private TestExecutor m_ownerExecutor;
    private TestExecutorActivity m_ownerActivity;

    public Collector(TestExecutorActivity testExecutorActivity ,TestExecutor testExecutor ,int number) {
        m_inputInterface = new InputInterface(testExecutorActivity ,this);
        m_numberOfMeasurements = number;
        m_ownerExecutor = testExecutor;
        m_ownerActivity = testExecutorActivity;
    }

    public void Start(InputInterface.InputTypeEnum inputTypeEnum){
        //TestExecutor запускает Collector, тот в свою очередь – Iinput::Start() (сенсор или считыватель из ЗУ).
        m_inputInterface.Start(inputTypeEnum);
    }

    public void Amass(){
        //метод Collector’a, чтобы он сохранял очередные значения датчиков
        // (три значения для трёх осей + временная отметка получения данных)
        // в структуру TestData::timeDomain (в соответствующие массивы X, Y, Z, T)
        //Также этот метод Collector’a проверяет, набралось ли достаточное количество
        // отсчётов (необходимое количество число хардкодом задано в нём, например 17000).
        m_count++;
        m_ownerActivity.setProgress(m_count); // ProgressBar заполняется по ходу накопления данных
        m_ownerExecutor.g_testData.XAxis[m_count] = 5d;

        // По окончании накопления необходимого количества отсчётов Collector останавливает IInput
        // и вызывает метод TestExecutor’a (или возвращает ему управление).
        // Далее TestExecutor вызывает метод Analyze() и передаёт ему ссылку на TestData
        m_ownerExecutor.Analyze(m_ownerExecutor.g_testData);
=======
    private int count;
    private TestExecutor m_testExecutor;
    private TestData m_testData;
    public void Start(boolean input, TestExecutor testExecutor){
        m_inputInterface = new InputInterface();
        m_testExecutor = testExecutor;
        if (input){
            m_inputInterface.Start(InputInterface.InputTypeEnum.sensors , this);
        } else{

        }
    }

    public void Collect(){
        //Заполняем тестДата
        count++;
        //после заполнения стоп
        m_inputInterface.Stop();
        m_testExecutor.Analyze(m_testData);
>>>>>>> 66531d8cde43049e596bd73b4650f29978cb631b
    }
}
