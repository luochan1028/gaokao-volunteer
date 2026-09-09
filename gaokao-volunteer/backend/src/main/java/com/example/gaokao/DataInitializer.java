package com.example.gaokao;

import com.example.gaokao.entity.*;
import com.example.gaokao.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final EnrollmentScoreRepository enrollmentScoreRepository;
    private final AdmissionPlanRepository admissionPlanRepository;
    private final PolicyDocumentRepository policyDocumentRepository;
    private final PasswordEncoder passwordEncoder;

    private List<College> colleges = new ArrayList<>();

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("数据已存在，跳过初始化");
            return;
        }

        log.info("========== 开始初始化模拟数据 ==========");

        initUsers();
        initColleges();
        initMajors();
        initEnrollmentScores();
        initAdmissionPlans();
        initPolicyDocuments();

        log.info("========== 模拟数据初始化完成！ ==========");
        log.info("用户: {} | 院校: {} | 专业: {} | 录取分数: {} | 招生计划: {} | 政策公告: {}",
                userRepository.count(), collegeRepository.count(), majorRepository.count(),
                enrollmentScoreRepository.count(), admissionPlanRepository.count(),
                policyDocumentRepository.count());
    }

    private void initUsers() {
        // D01: 3个预设考生画像
        User highScore = User.builder()
                .phone("13900000001").password(passwordEncoder.encode("123456"))
                .name("李高分").role(User.UserRole.ROLE_USER).userType(User.UserType.STUDENT)
                .province("四川").examYear("2024").totalScore(650).rank(800)
                .scienceOrArts("SCIENCE").selectedSubjects("物理,化学,生物")
                .subjectDetails("物理类").enabled(true).build();
        userRepository.save(highScore);

        User midScore = User.builder()
                .phone("13900000002").password(passwordEncoder.encode("123456"))
                .name("王中分").role(User.UserRole.ROLE_USER).userType(User.UserType.STUDENT)
                .province("四川").examYear("2024").totalScore(550).rank(15000)
                .scienceOrArts("SCIENCE").selectedSubjects("物理,地理,政治")
                .subjectDetails("物理类").enabled(true).build();
        userRepository.save(midScore);

        User lowScore = User.builder()
                .phone("13900000003").password(passwordEncoder.encode("123456"))
                .name("赵低分").role(User.UserRole.ROLE_USER).userType(User.UserType.STUDENT)
                .province("四川").examYear("2024").totalScore(480).rank(40000)
                .scienceOrArts("SCIENCE").selectedSubjects("物理,化学,地理")
                .subjectDetails("物理类").enabled(true).build();
        userRepository.save(lowScore);

        // 兼容旧测试账号
        User testUser = User.builder()
                .phone("13900000000").password(passwordEncoder.encode("123456"))
                .name("张三").role(User.UserRole.ROLE_USER).userType(User.UserType.STUDENT)
                .province("四川").examYear("2024").totalScore(620).rank(3000)
                .scienceOrArts("SCIENCE").selectedSubjects("物理,化学,生物")
                .subjectDetails("物理类").enabled(true).build();
        userRepository.save(testUser);

        User admin = User.builder()
                .phone("13800000000").password(passwordEncoder.encode("123456"))
                .name("管理员").role(User.UserRole.ROLE_ADMIN).enabled(true).build();
        userRepository.save(admin);

        log.info("✓ 用户数据初始化完成（5个用户：3个考生画像 + 测试账号 + 管理员）");
    }

    private void initColleges() {
        String[][] data = {
            {"清华大学","清华","10003","北京","北京","985"},
            {"北京大学","北大","10001","北京","北京","985"},
            {"复旦大学","复旦","10246","上海","上海","985"},
            {"上海交通大学","上交","10248","上海","上海","985"},
            {"浙江大学","浙大","10335","浙江","杭州","985"},
            {"南京大学","南大","10284","江苏","南京","985"},
            {"中国科学技术大学","中科大","10358","安徽","合肥","985"},
            {"华中科技大学","华科","10487","湖北","武汉","985"},
            {"武汉大学","武大","10486","湖北","武汉","985"},
            {"中山大学","中大","10558","广东","广州","985"},
            {"四川大学","川大","10610","四川","成都","985"},
            {"电子科技大学","电子科大","10614","四川","成都","985"},
            {"北京理工大学","北理","10007","北京","北京","985"},
            {"西安交通大学","西交","10698","陕西","西安","985"},
            {"哈尔滨工业大学","哈工大","10213","黑龙江","哈尔滨","985"},
            {"同济大学","同济","10247","上海","上海","985"},
            {"北京航空航天大学","北航","10006","北京","北京","985"},
            {"东南大学","东大","10286","江苏","南京","985"},
            {"南开大学","南开","10055","天津","天津","985"},
            {"天津大学","天大","10056","天津","天津","985"},
            {"山东大学","山大","10422","山东","济南","985"},
            {"中南大学","中南","10533","湖南","长沙","985"},
            {"厦门大学","厦大","10384","福建","厦门","985"},
            {"吉林大学","吉大","10183","吉林","长春","985"},
            {"华南理工大学","华工","10561","广东","广州","985"},
            {"重庆大学","重大","10611","重庆","重庆","985"},
            {"西南交通大学","西南交大","10613","四川","成都","211"},
            {"西南财经大学","西财","10651","四川","成都","211"},
            {"四川农业大学","川农大","10626","四川","雅安","211"},
            {"成都理工大学","成理","10616","四川","成都","双一流"},
            {"西南石油大学","西油","10615","四川","成都","双一流"},
            {"四川师范大学","川师","10636","四川","成都","普通本科"},
            {"西华大学","西华","10623","四川","成都","普通本科"},
            {"成都信息工程大学","成信","10621","四川","成都","普通本科"},
            {"西南民族大学","西南民大","10656","四川","成都","普通本科"},
            {"成都大学","成大","11079","四川","成都","普通本科"},
            {"西华师范大学","西华师范","10638","四川","南充","普通本科"},
            {"绵阳师范学院","绵阳师院","10640","四川","绵阳","普通本科"},
            {"攀枝花学院","攀大","11360","四川","攀枝花","普通本科"},
            {"宜宾学院","宜院","10641","四川","宜宾","普通本科"},
            {"西南大学","西大","10635","重庆","重庆","211"},
            {"贵州大学","贵大","10657","贵州","贵阳","211"},
            {"云南大学","云大","10673","云南","昆明","211"},
            {"西北大学","西大","10697","陕西","西安","211"},
            {"陕西师范大学","陕师大","10718","陕西","西安","211"},
            {"长安大学","长大","10710","陕西","西安","211"},
            {"兰州大学","兰大","10730","甘肃","兰州","985"},
            {"中国矿业大学","矿大","10290","江苏","徐州","211"},
            {"中国地质大学","地大","10491","湖北","武汉","211"},
            {"华中师范大学","华师","10511","湖北","武汉","211"}
        };

        for (String[] row : data) {
            boolean is985 = row[5].equals("985");
            boolean is211 = row[5].equals("985") || row[5].equals("211");
            boolean isDoubleFirst = row[5].equals("985") || row[5].equals("211") || row[5].equals("双一流");

            College college = College.builder()
                    .name(row[0]).shortName(row[1]).code(row[2])
                    .province(row[3]).city(row[4])
                    .type(College.CollegeType.PUBLIC)
                    .level(College.CollegeLevel.UNIVERSITY)
                    .is985(is985).is211(is211).isDoubleFirstClass(isDoubleFirst)
                    .hasGraduateProgram(true)
                    .satisfactionScore(4.0 + Math.random() * 1.0)
                    .studentCount(15000 + (int)(Math.random() * 15000))
                    .teacherCount(1000 + (int)(Math.random() * 2000))
                    .description(row[0] + "是一所在" + row[3] + row[4] + "的" + row[5] + "院校")
                    .features("学术氛围浓厚,师资力量雄厚,就业前景好")
                    .advantages("多学科协调发展,科研实力强")
                    .foundedYear("1900")
                    .website("https://www." + row[1] + ".edu.cn")
                    .phone("010-12345678")
                    .build();
            collegeRepository.save(college);
            colleges.add(college);
        }

        log.info("✓ 院校数据初始化完成，共{}所", collegeRepository.count());
    }

    private void initMajors() {
        String[][] data = {
            {"计算机科学与技术","080901","工学","计算机类","ENGINEERING","物理","数据结构,操作系统,计算机网络,算法设计,数据库原理","软件开发,系统架构,人工智能,数据分析","软件工程师,算法工程师,系统架构师","true","true"},
            {"软件工程","080902","工学","软件工程类","ENGINEERING","物理","软件体系结构,软件测试,软件项目管理,需求工程","软件开发,项目管理,质量保证","软件工程师,项目经理,测试工程师","true","true"},
            {"人工智能","080717","工学","人工智能类","ENGINEERING","物理","机器学习,深度学习,自然语言处理,计算机视觉","AI研发,智能系统,数据分析","AI工程师,算法专家,数据科学家","true","false"},
            {"电子信息工程","080701","工学","电子信息类","ENGINEERING","物理","信号与系统,数字电路,通信原理,嵌入式系统","电子设备研发,通信系统,智能硬件","电子工程师,通信工程师,硬件架构师","true","true"},
            {"通信工程","080703","工学","通信类","ENGINEERING","物理","通信原理,移动通信,光纤通信,网络协议","通信系统设计,网络优化,5G研发","通信工程师,网络规划师","true","false"},
            {"机械工程","080201","工学","机械类","ENGINEERING","物理","机械设计,制造技术,材料力学,控制工程","机械设计,制造自动化,机器人","机械工程师,设计工程师","true","true"},
            {"自动化","080801","工学","自动化类","ENGINEERING","物理","控制理论,传感器技术,PLC编程,机器人技术","工业自动化,智能制造,机器人","自动化工程师,控制工程师","true","false"},
            {"电气工程及其自动化","080601","工学","电气类","ENGINEERING","物理","电路原理,电机学,电力系统,电力电子","电力系统,电气设备,新能源","电气工程师,电力工程师","true","true"},
            {"土木工程","081001","工学","土木类","ENGINEERING","物理","结构力学,混凝土结构,土力学,施工技术","建筑设计,工程施工,项目管理","土木工程师,建造师","true","false"},
            {"建筑学","082801","工学","建筑类","ENGINEERING","物理","建筑设计,建筑历史,城市规划,室内设计","建筑设计,城市规划,景观设计","建筑师,规划师,设计师","false","true"},
            {"数学与应用数学","070101","理学","数学类","SCIENCE","物理","数学分析,高等代数,概率统计,微分方程","教育,科研,金融分析,数据分析","数学教师,数据分析师,精算师","true","true"},
            {"物理学","070201","理学","物理学类","SCIENCE","物理","力学,电磁学,光学,量子力学","科研,教育,半导体,光电","物理学家,研发工程师","true","true"},
            {"化学","070301","理学","化学类","SCIENCE","物理或化学","有机化学,无机化学,物理化学,分析化学","科研,化工,材料,环保","化学家,材料工程师","true","true"},
            {"生物科学","071001","理学","生物科学类","SCIENCE","物理或化学","细胞生物学,遗传学,生态学,分子生物学","生物制药,科研,检验","生物学家,研发工程师","false","false"},
            {"临床医学","100201","医学","临床医学类","MEDICINE","物理或化学","解剖学,生理学,病理学,内科学,外科学","医院临床,医学研究,医疗管理","临床医师,医学研究员","true","true"},
            {"口腔医学","100301","医学","口腔医学类","MEDICINE","物理或化学","口腔解剖,口腔病理,口腔修复,口腔正畸","口腔临床,口腔美容","口腔医师","true","false"},
            {"护理学","101101","医学","护理学类","MEDICINE","不限","护理学基础,内科护理,外科护理,急救护理","医院护理,社区护理,护理管理","护士,护理主管","false","false"},
            {"法学","030101","法学","法学类","LAW","历史","宪法,民法,刑法,行政法,经济法","法律实务,公务员,企业法务","律师,法官,检察官","true","true"},
            {"政治学与行政学","030201","法学","政治学类","LAW","历史或政治","政治学原理,行政学,公共政策,政治制度","政府机关,企事业单位","公务员,行政人员","false","false"},
            {"汉语言文学","050101","文学","中国语言文学类","LITERATURE","历史","古代汉语,现代汉语,中国古代文学,文学理论","教育,文化传媒,出版","教师,编辑,文案","false","true"},
            {"新闻学","050301","文学","新闻传播学类","LITERATURE","历史或政治","新闻采访,新闻写作,新闻编辑,传播学","新闻媒体,新媒体,公关","记者,编辑,新媒体运营","false","false"},
            {"英语","050201","文学","外国语言文学类","LITERATURE","不限","综合英语,英语听力,翻译,英美文学","翻译,教育,外贸,涉外","翻译,英语教师","false","false"},
            {"经济学","020101","经济学","经济学类","ECONOMICS","物理或历史","微观经济学,宏观经济学,计量经济学,金融学","银行,证券,政府,企业","经济分析师,金融顾问","true","true"},
            {"金融学","020301","经济学","金融学类","ECONOMICS","物理或历史","货币银行学,投资学,公司金融,金融市场","银行,基金,证券,保险","金融分析师,投资顾问","true","false"},
            {"会计学","120203","管理学","工商管理类","MANAGEMENT","不限","财务会计,管理会计,审计学,税法","企业财务,审计,税务","会计师,审计师","true","false"},
            {"工商管理","120201","管理学","工商管理类","MANAGEMENT","不限","管理学,市场营销,人力资源管理,运营管理","企业管理,市场营销,咨询","管理顾问,市场经理","false","false"},
            {"电子商务","120801","管理学","电子商务类","MANAGEMENT","不限","电子商务概论,网络营销,供应链,电商运营","电商运营,网络营销,跨境电商","电商运营,数字营销","true","false"},
            {"物流管理","120601","管理学","物流管理与工程类","MANAGEMENT","不限","物流学,供应链管理,仓储配送,物流系统","物流,供应链,采购","物流经理,供应链管理","false","false"},
            {"教育学","040101","教育学","教育学类","EDUCATION","不限","教育学原理,课程论,教学论,教育心理学","学校教育,教育培训","教师,教育管理","false","false"},
            {"学前教育","040106","教育学","教育学类","EDUCATION","不限","学前心理学,幼儿教育,游戏理论,幼儿体育","幼儿园,早教机构","幼儿教师","false","false"}
        };

        // Assign majors to colleges
        int majorIdx = 0;
        for (String[] row : data) {
            // Each major assigned to multiple colleges
            int assignCount = 3 + (int)(Math.random() * 5);
            List<Integer> assigned = new ArrayList<>();
            for (int j = 0; j < assignCount && j < colleges.size(); j++) {
                int collegeIdx = (majorIdx * 3 + j * 7) % colleges.size();
                if (assigned.contains(collegeIdx)) continue;
                assigned.add(collegeIdx);
                College college = colleges.get(collegeIdx);

                Major major = Major.builder()
                        .name(row[0]).code(row[1])
                        .subject(row[2])
                        .category(Major.MajorCategory.valueOf(row[4]))
                        .subjectRequirements(row[5])
                        .description(row[0] + "专业，属于" + row[2] + row[3])
                        .coreCourses(row[6])
                        .employmentDirections(row[7])
                        .relatedOccupations(row[8])
                        .isHot(Boolean.parseBoolean(row[9]))
                        .isNationalKey(Boolean.parseBoolean(row[10]))
                        .satisfactionScore(3.8 + Math.random() * 1.2)
                        .averageSalary(8000 + (int)(Math.random() * 15000))
                        .employmentRate(0.85 + Math.random() * 0.14)
                        .college(college)
                        .careerProspects("就业前景广阔,行业需求持续增长")
                        .build();
                majorRepository.save(major);
            }
            majorIdx++;
        }

        log.info("✓ 专业数据初始化完成，共{}个", majorRepository.count());
    }

    private void initEnrollmentScores() {
        // 为每所院校生成四川3年（2022-2024）的录取数据
        String[] years = {"2022", "2023", "2024"};
        String province = "四川";

        for (College college : colleges) {
            for (String year : years) {
                // 理科
                int baseScore = college.getIs985() ? 600 : (college.getIs211() ? 550 : (college.getIsDoubleFirstClass() ? 520 : 480));
                int yearOffset = Integer.parseInt(year) - 2022;
                baseScore += yearOffset * 3;

                int minScore = baseScore - 15;
                int maxScore = baseScore + 25;
                int avgScore = baseScore + 5;
                int minRank = college.getIs985() ? 500 : (college.getIs211() ? 5000 : (college.getIsDoubleFirstClass() ? 15000 : 30000));
                int maxRank = minRank + 3000;
                int avgRank = minRank + 1500;
                int enrollCount = 30 + (int)(Math.random() * 70);

                EnrollmentScore scienceScore = EnrollmentScore.builder()
                        .year(year).province(province)
                        .batch(EnrollmentScore.Batch.BATCH_A)
                        .scienceOrArts(EnrollmentScore.ScienceOrArts.SCIENCE)
                        .minScore(minScore).maxScore(maxScore).averageScore(avgScore)
                        .minRank(minRank).maxRank(maxRank).averageRank(avgRank)
                        .enrollmentCount(enrollCount)
                        .college(college)
                        .build();
                enrollmentScoreRepository.save(scienceScore);

                // 文科（部分院校）
                if (college.getIs985() || college.getIs211() || Math.random() > 0.5) {
                    int artsBase = baseScore - 30;
                    EnrollmentScore artsScore = EnrollmentScore.builder()
                            .year(year).province(province)
                            .batch(EnrollmentScore.Batch.BATCH_A)
                            .scienceOrArts(EnrollmentScore.ScienceOrArts.ARTS)
                            .minScore(artsBase - 15).maxScore(artsBase + 25).averageScore(artsBase + 5)
                            .minRank(minRank + 2000).maxRank(minRank + 5000).averageRank(minRank + 3500)
                            .enrollmentCount(20 + (int)(Math.random() * 50))
                            .college(college)
                            .build();
                    enrollmentScoreRepository.save(artsScore);
                }
            }
        }

        // 添加一些北京的历史数据（兼容旧逻辑）
        for (int i = 0; i < Math.min(12, colleges.size()); i++) {
            College college = colleges.get(i);
            EnrollmentScore bjScore = EnrollmentScore.builder()
                    .year("2023").province("北京")
                    .batch(EnrollmentScore.Batch.SPECIAL_BATCH)
                    .scienceOrArts(EnrollmentScore.ScienceOrArts.SCIENCE)
                    .minScore(620 + i * 5).maxScore(700 + i * 5).averageScore(660 + i * 5)
                    .minRank(100 + i * 150).maxRank(400 + i * 200).averageRank(250 + i * 175)
                    .enrollmentCount(40 + i * 10)
                    .college(college)
                    .build();
            enrollmentScoreRepository.save(bjScore);
        }

        log.info("✓ 录取分数数据初始化完成，共{}条", enrollmentScoreRepository.count());
    }

    private void initAdmissionPlans() {
        // 为每所院校生成2026年四川招生计划
        String[] majorNames = {"计算机科学与技术", "软件工程", "电子信息工程", "机械工程", "电气工程及其自动化",
                "土木工程", "数学与应用数学", "物理学", "化学", "临床医学",
                "法学", "汉语言文学", "新闻学", "经济学", "金融学",
                "会计学", "工商管理", "英语", "自动化", "人工智能"};
        String[] majorCodes = {"080901", "080902", "080701", "080201", "080601",
                "081001", "070101", "070201", "070301", "100201",
                "030101", "050101", "050301", "020101", "020301",
                "120203", "120201", "050201", "080801", "080717"};
        String[] subjectReqs = {"物理", "物理", "物理", "物理", "物理",
                "物理", "物理", "物理", "物理或化学", "物理或化学",
                "历史", "历史", "历史或政治", "物理或历史", "物理或历史",
                "不限", "不限", "不限", "物理", "物理"};
        String[] schoolSystems = {"4", "4", "4", "4", "4", "4", "4", "4", "4", "5", "4", "4", "4", "4", "4", "4", "4", "4", "4", "4"};
        int[] tuitions = {4500, 5000, 4500, 4500, 4500, 4500, 4500, 4500, 4500, 6000, 4500, 4500, 4500, 4500, 5000, 4500, 4500, 4500, 4500, 5500};

        for (College college : colleges) {
            int planCount = 3 + (int)(Math.random() * 5);
            for (int i = 0; i < planCount; i++) {
                int idx = (int)(Math.random() * majorNames.length);
                AdmissionPlan plan = AdmissionPlan.builder()
                        .year("2026").province("四川")
                        .batch(AdmissionPlan.Batch.BATCH_A)
                        .scienceOrArts("SCIENCE")
                        .collegeCode(college.getCode())
                        .majorCode(majorCodes[idx])
                        .majorName(majorNames[idx])
                        .enrollmentCount(5 + (int)(Math.random() * 45))
                        .schoolSystem(schoolSystems[idx])
                        .tuition(tuitions[idx] + (int)(Math.random() * 2000))
                        .subjectRequirements(subjectReqs[idx])
                        .isSimulated(true)
                        .college(college)
                        .build();
                admissionPlanRepository.save(plan);
            }
        }

        log.info("✓ 招生计划数据初始化完成，共{}条", admissionPlanRepository.count());
    }

    private void initPolicyDocuments() {
        String[][] policies = {
            {"2024年四川省高考志愿填报时间安排", "四川", "EXAM_POLICY",
             "根据四川省教育考试院安排，2024年高考志愿填报时间为6月23日至7月5日。本科提前批填报截止时间为6月26日12:00，本科批填报截止时间为7月5日18:00。请考生务必在规定时间内完成填报，逾期不再补报。",
             "四川省教育考试院", "填报时间,本科批,提前批"},
            {"2024年四川省高考录取批次规则说明", "四川", "ENROLLMENT_RULE",
             "四川省2024年高考录取批次分为：本科提前批、本科第一批、本科第二批、专科提前批、专科批。本科批实行平行志愿，每批次设置6个院校志愿，每个院校志愿设置6个专业志愿和专业调配志愿。",
             "四川省教育考试院", "批次,平行志愿,院校志愿"},
            {"新高考选科要求指引（2024年）", "四川", "MAJOR_INTRO",
             "根据教育部最新指引，理工类专业多数要求选考物理，部分要求物理+化学。人文社科类专业一般不限制选考科目。医学类专业通常要求物理或化学。具体选科要求以各院校招生简章为准。",
             "教育部", "选科,物理,化学,医学"},
            {"2024年各批次录取分数线公告", "四川", "SCORE_LINE",
             "2024年四川省高考录取分数线：理科本科一批539分，本科二批459分；文科本科一批529分，本科二批457分。专科批文理科均为150分。",
             "四川省教育考试院", "分数线,本科一批,本科二批"},
            {"高考志愿填报注意事项", "四川", "OTHER",
             "1.认真阅读招生章程，了解院校录取规则；2.合理安排志愿梯度，建议冲稳保比例3:5:2；3.注意专业对身体条件的要求；4.关注征集志愿信息；5.保管好账号密码，防止志愿被篡改。",
             "四川省教育考试院", "注意事项,志愿梯度,征集志愿"}
        };

        for (int i = 0; i < policies.length; i++) {
            String[] row = policies[i];
            PolicyDocument doc = new PolicyDocument();
            doc.setTitle(row[0]);
            doc.setProvince(row[1]);
            doc.setPolicyType(PolicyDocument.PolicyType.valueOf(row[2]));
            doc.setContent(row[3]);
            doc.setSource(row[4]);
            doc.setTags(row[5]);
            doc.setIsActive(true);
            doc.setPublishDate(LocalDateTime.now().minusDays(policies.length - i));
            policyDocumentRepository.save(doc);
        }

        log.info("✓ 政策公告数据初始化完成，共{}条", policyDocumentRepository.count());
    }


}
