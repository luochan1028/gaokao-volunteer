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
    private final com.example.gaokao.repository.SchoolMajorGroupRepository schoolMajorGroupRepository;
    private final com.example.gaokao.repository.MajorCareerNodeRepository majorCareerNodeRepository;
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

        initV2SchoolMajorGroups();
        initV2MajorCareerNodes();

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
        // 全国主要省份及难度系数（系数越大，同档次院校分数越高）
        String[][] provinceData = {
            {"北京", "0.95", "0.3"},    // 北京：分数略低，位次靠前（考生少）
            {"上海", "0.97", "0.4"},    // 上海：分数中等
            {"江苏", "1.02", "1.2"},    // 江苏：高考大省，竞争激烈
            {"浙江", "1.00", "1.0"},    // 浙江：中等
            {"广东", "1.01", "1.3"},    // 广东：考生多
            {"四川", "0.98", "1.1"},    // 四川：考生较多
            {"湖北", "0.99", "0.9"},    // 湖北：中等
            {"山东", "1.00", "1.4"},    // 山东：高考大省
            {"河南", "1.03", "1.5"},    // 河南：竞争最激烈
            {"湖南", "0.99", "1.0"},    // 湖南：中等
            {"安徽", "1.00", "1.1"},    // 安徽：考生较多
            {"陕西", "0.97", "0.8"}     // 陕西：教育资源较好
        };
        String[] years = {"2022", "2023", "2024"};

        int scoreCount = 0;
        for (String[] prov : provinceData) {
            String province = prov[0];
            double scoreFactor = Double.parseDouble(prov[1]);
            double rankFactor = Double.parseDouble(prov[2]);

            for (College college : colleges) {
                for (String year : years) {
                    int baseScore = college.getIs985() ? 600 : (college.getIs211() ? 550 : (college.getIsDoubleFirstClass() ? 520 : 480));
                    int yearOffset = Integer.parseInt(year) - 2022;
                    baseScore = (int)(baseScore * scoreFactor) + yearOffset * 3;

                    int minScore = baseScore - 15;
                    int maxScore = baseScore + 25;
                    int avgScore = baseScore + 5;
                    int baseRank = college.getIs985() ? 500 : (college.getIs211() ? 5000 : (college.getIsDoubleFirstClass() ? 15000 : 30000));
                    int minRank = (int)(baseRank * rankFactor);
                    int maxRank = minRank + (int)(3000 * rankFactor);
                    int avgRank = minRank + (int)(1500 * rankFactor);
                    int enrollCount = 20 + (int)(Math.random() * 60);

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
                    scoreCount++;

                    // 文科（重点院校都有，普通院校50%概率）
                    if (college.getIs985() || college.getIs211() || Math.random() > 0.5) {
                        int artsBase = (int)(baseScore * 0.95);
                        EnrollmentScore artsScore = EnrollmentScore.builder()
                                .year(year).province(province)
                                .batch(EnrollmentScore.Batch.BATCH_A)
                                .scienceOrArts(EnrollmentScore.ScienceOrArts.ARTS)
                                .minScore(artsBase - 15).maxScore(artsBase + 20).averageScore(artsBase + 3)
                                .minRank(minRank + (int)(2000 * rankFactor)).maxRank(minRank + (int)(5000 * rankFactor)).averageRank(minRank + (int)(3500 * rankFactor))
                                .enrollmentCount(15 + (int)(Math.random() * 40))
                                .college(college)
                                .build();
                        enrollmentScoreRepository.save(artsScore);
                        scoreCount++;
                    }
                }
            }
        }

        log.info("✓ 录取分数数据初始化完成，共{}条（12个省份 × 3年 × 文理）", scoreCount);
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

    private void initV2SchoolMajorGroups() {
        if (schoolMajorGroupRepository != null && schoolMajorGroupRepository.count() > 0) {
            log.info("V2.0院校专业组数据已存在，跳过初始化");
            return;
        }

        // 四川院校专业组数据
        Object[][] data = {
            {"四川大学", "10610", "四川", "985", "SC001", "计算机类", "[\"计算机科学与技术\",\"软件工程\",\"数据科学\"]", "物理+化学", 625, 620, 615, 8000, 8500, 9000, true, 5000},
            {"四川大学", "10610", "四川", "985", "SC002", "医学类", "[\"临床医学\",\"口腔医学\"]", "物理+化学", 640, 635, 630, 4000, 4200, 4500, true, 6000},
            {"四川大学", "10610", "四川", "985", "SC003", "法学类", "[\"法学\"]", "历史", 610, 605, 600, 12000, 12500, 13000, true, 4500},
            {"电子科技大学", "10614", "四川", "985", "SC004", "电子信息类", "[\"电子信息工程\",\"通信工程\"]", "物理+化学", 635, 630, 625, 5000, 5500, 6000, true, 5500},
            {"电子科技大学", "10614", "四川", "985", "SC005", "计算机类", "[\"计算机科学与技术\",\"人工智能\"]", "物理+化学", 640, 635, 630, 4000, 4500, 5000, true, 5500},
            {"西南交通大学", "10613", "四川", "211", "SC006", "土木类", "[\"土木工程\",\"建筑学\"]", "物理+化学", 580, 575, 570, 20000, 21000, 22000, true, 4500},
            {"西南交通大学", "10613", "四川", "211", "SC007", "电气类", "[\"电气工程及其自动化\"]", "物理+化学", 590, 585, 580, 18000, 19000, 20000, true, 4500},
            {"西南财经大学", "10651", "四川", "211", "SC008", "金融类", "[\"金融学\",\"经济学\"]", "物理或历史", 595, 590, 585, 15000, 16000, 17000, true, 5000},
            {"西南财经大学", "10651", "四川", "211", "SC009", "会计类", "[\"会计学\",\"财务管理\"]", "不限", 580, 575, 570, 20000, 21000, 22000, true, 5000},
            {"四川农业大学", "10626", "四川", "211", "SC010", "农学类", "[\"农学\",\"动物医学\"]", "物理或化学", 530, 525, 520, 50000, 52000, 54000, true, 4500},
            {"成都理工大学", "10616", "四川", "双一流", "SC011", "地质类", "[\"地质学\",\"地球物理\"]", "物理+化学", 540, 535, 530, 40000, 42000, 44000, true, 4500},
            {"成都理工大学", "10616", "四川", "双一流", "SC012", "计算机类", "[\"计算机科学与技术\"]", "物理+化学", 550, 545, 540, 35000, 37000, 39000, false, 4500},
            {"西南石油大学", "10615", "四川", "双一流", "SC013", "石油类", "[\"石油工程\",\"油气储运\"]", "物理+化学", 530, 525, 520, 50000, 52000, 54000, true, 4500},
            {"四川师范大学", "10636", "四川", "普通本科", "SC014", "师范类", "[\"汉语言文学\",\"数学与应用数学\",\"英语\"]", "不限", 520, 515, 510, 55000, 57000, 59000, true, 3700},
            {"四川师范大学", "10636", "四川", "普通本科", "SC015", "教育学类", "[\"教育学\",\"学前教育\"]", "不限", 510, 505, 500, 60000, 62000, 64000, true, 3700},
            {"西华大学", "10623", "四川", "普通本科", "SC016", "机械类", "[\"机械工程\",\"自动化\"]", "物理+化学", 515, 510, 505, 58000, 60000, 62000, true, 4500},
            {"西华大学", "10623", "四川", "普通本科", "SC017", "食品类", "[\"食品科学与工程\"]", "物理或化学", 500, 495, 490, 70000, 72000, 74000, false, 4500},
            {"成都信息工程大学", "10621", "四川", "普通本科", "SC018", "计算机类", "[\"计算机科学与技术\",\"软件工程\"]", "物理+化学", 525, 520, 515, 50000, 52000, 54000, true, 4500},
            {"成都信息工程大学", "10621", "四川", "普通本科", "SC019", "气象类", "[\"大气科学\"]", "物理+化学", 510, 505, 500, 65000, 67000, 69000, true, 4500},
            {"成都大学", "11079", "四川", "普通本科", "SC020", "综合类", "[\"工商管理\",\"旅游管理\"]", "不限", 495, 490, 485, 75000, 77000, 79000, false, 4500},
            {"西南民族大学", "10656", "四川", "普通本科", "SC021", "法学类", "[\"法学\"]", "不限", 510, 505, 500, 60000, 62000, 64000, true, 4500},
            {"西南民族大学", "10656", "四川", "普通本科", "SC022", "文学类", "[\"汉语言文学\",\"新闻学\"]", "不限", 500, 495, 490, 70000, 72000, 74000, false, 4500},
        };

        for (Object[] row : data) {
            SchoolMajorGroup g = SchoolMajorGroup.builder()
                .schoolName((String) row[0])
                .schoolCode((String) row[1])
                .schoolProvince((String) row[2])
                .schoolTier((String) row[3])
                .majorGroupCode((String) row[4])
                .majorGroupName((String) row[5])
                .majorList((String) row[6])
                .subjectRequirement((String) row[7])
                .batch("本科批")
                .admissionLine2024((Integer) row[8])
                .admissionLine2023((Integer) row[9])
                .admissionLine2022((Integer) row[10])
                .minRank2024((Integer) row[11])
                .minRank2023((Integer) row[12])
                .minRank2022((Integer) row[13])
                .hasMasterPoint((Boolean) row[14])
                .tuitionFee((Integer) row[15])
                .isDiscontinued(false)
                .isChanged(false)
                .dataInsufficient(false)
                .build();
            schoolMajorGroupRepository.save(g);
        }
        log.info("✓ V2.0院校专业组数据初始化完成");
    }

    private void initV2MajorCareerNodes() {
        if (majorCareerNodeRepository != null && majorCareerNodeRepository.count() > 0) {
            log.info("V2.0专业职业节点数据已存在，跳过初始化");
            return;
        }

        Object[][] data = {
            {"计算机科学与技术", "080901", "工学", "[\"软件开发工程师\",\"算法工程师\",\"运维工程师\"]", "{\"互联网\":40,\"金融\":15,\"制造业\":20}", "10-18K", "8-12K", "5-8K", "15-25K", "[\"初级开发→高级开发→架构师→技术总监\"]", "[\"计算机技术\",\"软件工程\",\"人工智能\"]", "[\"各部委信息中心\",\"税务局\",\"统计局\"]", "[\"软考\",\"PMP\"]", "低", "风口", "I", "AI时代核心专业，需求持续旺盛"},
            {"软件工程", "080902", "工学", "[\"软件工程师\",\"项目经理\",\"测试工程师\"]", "{\"互联网\":45,\"金融\":10,\"通信\":15}", "10-16K", "8-10K", "5-7K", "15-22K", "[\"开发→高级开发→项目经理→技术管理\"]", "[\"软件工程\",\"计算机技术\"]", "[\"税务局\",\"海关\"]", "[\"软考\"]", "低", "风口", "I", "工程化能力突出，就业面广"},
            {"临床医学", "100201", "医学", "[\"临床医师\",\"医学研究员\",\"医疗管理\"]", "{\"医疗\":80,\"教育\":10}", "8-12K", "6-8K", "4-6K", "15-30K", "[\"住院医师→主治医师→副主任医师→主任医师\"]", "[\"内科学\",\"外科学\"]", "[\"卫健委\",\"疾控中心\"]", "[\"执业医师证\"]", "低", "普通家庭慎选", "I", "长学制长回报，需硕士起步，普通家庭经济压力大"},
            {"法学", "030101", "法学", "[\"律师\",\"法官\",\"企业法务\"]", "{\"法律服务\":40,\"政府\":25,\"企业\":25}", "6-10K", "5-8K", "3-5K", "12-20K", "[\"实习律师→执业律师→合伙人→高级合伙人\"]", "[\"法学\",\"法律硕士\"]", "[\"法院\",\"检察院\",\"司法局\"]", "[\"法律职业资格证\"]", "中", "常规", "S", "考公优势大，但法考通过率仅15%左右"},
            {"金融学", "020301", "经济学", "[\"金融分析师\",\"投资顾问\",\"银行客户经理\"]", "{\"银行\":30,\"证券\":20,\"基金\":15}", "8-15K", "6-10K", "4-6K", "15-25K", "[\"柜员→客户经理→分支行长\"]", "[\"金融学\",\"金融硕士\"]", "[\"银保监会\",\"税务局\"]", "[\"CFA\",\"基金从业\"]", "高", "普通家庭慎选", "E", "资源导向型行业，无资源家庭发展受限"},
            {"会计学", "120203", "管理学", "[\"会计师\",\"审计师\",\"财务总监\"]", "{\"企业\":35,\"会计所\":25,\"政府\":15}", "6-9K", "5-7K", "3-5K", "12-18K", "[\"出纳→会计→财务经理→CFO\"]", "[\"会计学\",\"MPAcc\"]", "[\"财政局\",\"税务局\",\"审计署\"]", "[\"CPA\",\"初中级会计\"]", "高", "常规", "C", "AI替代风险高，基础会计岗位正在缩减"},
            {"汉语言文学", "050101", "文学", "[\"教师\",\"编辑\",\"文案策划\"]", "{\"教育\":40,\"媒体\":20,\"企业\":20}", "5-8K", "4-6K", "3-5K", "8-15K", "[\"教师→高级教师→教研员\"]", "[\"学科教学\"]", "[\"宣传部\",\"文化局\"]", "[\"教师资格证\"]", "中", "常规", "A", "万金油专业但竞争激烈，考公考编是主要出路"},
            {"电气工程及其自动化", "080601", "工学", "[\"电气工程师\",\"电力工程师\"]", "{\"电力\":40,\"制造\":25,\"建筑\":15}", "7-12K", "6-9K", "4-6K", "12-20K", "[\"助理工程师→工程师→高级工程师\"]", "[\"电气工程\"]", "[\"国家电网\",\"南方电网\"]", "[\"注册电气工程师\"]", "低", "风口", "R", "国家电网是最大雇主，稳定性强"},
            {"机械工程", "080201", "工学", "[\"机械工程师\",\"设计工程师\"]", "{\"制造\":50,\"汽车\":20,\"航空\":10}", "6-10K", "5-8K", "3-5K", "10-18K", "[\"技术员→工程师→高级工程师→总工\"]", "[\"机械工程\"]", "[\"工信厅\"]", "[\"注册机械工程师\"]", "中", "常规", "R", "传统工科，智能制造转型带来新机会"},
            {"土木工程", "081001", "工学", "[\"土木工程师\",\"建造师\",\"监理\"]", "{\"建筑\":40,\"基建\":30,\"政府\":15}", "6-9K", "5-7K", "3-5K", "10-16K", "[\"技术员→工程师→项目经理→总工\"]", "[\"土木工程\"]", "[\"住建局\",\"交通局\"]", "[\"一级建造师\"]", "低", "天坑", "R", "房地产下行周期，行业岗位大幅减少"},
            {"数学与应用数学", "070101", "理学", "[\"数据分析师\",\"精算师\",\"数学教师\"]", "{\"教育\":30,\"金融\":20,\"互联网\":20}", "7-12K", "6-9K", "4-6K", "12-20K", "[\"分析师→高级分析师→数据科学家\"]", "[\"应用数学\",\"统计学\"]", "[\"统计局\"]", "[\"精算师\"]", "低", "风口", "I", "基础学科，转行互联网/金融优势大"},
            {"护理学", "101101", "医学", "[\"护士\",\"护理主管\"]", "{\"医疗\":85,\"社区\":10}", "5-7K", "4-6K", "3-5K", "8-12K", "[\"护士→护师→主管护师→护士长\"]", "[\"护理学\"]", "[\"卫健委\"]", "[\"护士执业证\"]", "低", "常规", "S", "需求量大但工作强度高，职业天花板较低"},
            {"教育学", "040101", "教育学", "[\"教师\",\"教育管理\"]", "{\"教育\":70,\"政府\":15}", "5-8K", "4-6K", "3-5K", "8-14K", "[\"教师→教务主任→校长\"]", "[\"教育学\"]", "[\"教育局\"]", "[\"教师资格证\"]", "中", "常规", "S", "师范类专业稳定但薪资一般，考编是关键"},
            {"人工智能", "080717", "工学", "[\"AI工程师\",\"算法专家\"]", "{\"互联网\":50,\"AI企业\":30,\"金融\":10}", "12-20K", "10-15K", "6-10K", "20-35K", "[\"初级AI工程师→高级→算法专家→首席科学家\"]", "[\"人工智能\",\"计算机科学\"]", "[\"科技部\"]", "[\"深度学习认证\"]", "低", "风口", "I", "当前最火热方向，薪资高但竞争激烈"},
            {"农学", "090101", "农学", "[\"农业技术员\",\"农技推广\"]", "{\"农业\":60,\"政府\":20}", "4-6K", "3-5K", "3-4K", "6-10K", "[\"技术员→农艺师→高级农艺师\"]", "[\"作物学\"]", "[\"农业农村厅\"]", "[\"农艺师证\"]", "低", "天坑", "R", "就业面窄薪资低，但对口考公有优势"},
        };

        for (Object[] row : data) {
            MajorCareerNode n = MajorCareerNode.builder()
                .majorName((String) row[0])
                .majorCode((String) row[1])
                .category((String) row[2])
                .typicalJobs((String) row[3])
                .industryDistribution((String) row[4])
                .salaryTier1((String) row[5])
                .salaryTier2((String) row[6])
                .salaryTier3((String) row[7])
                .salary5YearMedian((String) row[8])
                .careerPath5Year((String) row[9])
                .postgradDirections((String) row[10])
                .civilServiceJobs((String) row[11])
                .requiredCerts((String) row[12])
                .aiReplaceRisk((String) row[13])
                .realityTag((String) row[14])
                .hollandCode((String) row[15])
                .realityReason((String) row[16])
                .build();
            majorCareerNodeRepository.save(n);
        }
        log.info("✓ V2.0专业职业节点数据初始化完成");
    }


}
