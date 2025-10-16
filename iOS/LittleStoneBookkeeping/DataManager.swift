import Foundation

class DataManager {
    
    static let shared = DataManager()
    
    private let transactionsKey = "transactions"
    private let categoriesKey = "categories"
    
    private init() {}
    
    // MARK: - Transaction Management
    
    func saveTransaction(_ transaction: Transaction) {
        var transactions = getAllTransactions()
        transactions.append(transaction)
        saveTransactions(transactions)
    }
    
    func getAllTransactions() -> [Transaction] {
        guard let data = UserDefaults.standard.data(forKey: transactionsKey) else {
            return []
        }
        
        do {
            let decoder = JSONDecoder()
            let transactions = try decoder.decode([Transaction].self, from: data)
            return transactions.sorted { $0.date > $1.date } // 按时间倒序排列
        } catch {
            print("Failed to decode transactions: \(error)")
            return []
        }
    }
    
    func deleteTransaction(_ id: UUID) {
        var transactions = getAllTransactions()
        transactions.removeAll { $0.id == id }
        saveTransactions(transactions)
    }
    
    func getTransactions(for date: Date) -> [Transaction] {
        let calendar = Calendar.current
        let transactions = getAllTransactions()
        
        return transactions.filter { transaction in
            calendar.isDate(transaction.date, inSameDayAs: date)
        }
    }
    
    func getTransactions(forMonth month: Date) -> [Transaction] {
        let calendar = Calendar.current
        let transactions = getAllTransactions()
        
        return transactions.filter { transaction in
            calendar.isDate(transaction.date, equalTo: month, toGranularity: .month)
        }
    }
    
    func getTransactions(forCategory category: Category) -> [Transaction] {
        let transactions = getAllTransactions()
        return transactions.filter { $0.category.id == category.id }
    }
    
    // MARK: - Statistics
    
    func getMonthlyStatistics(for date: Date) -> (income: Double, expense: Double, balance: Double) {
        let transactions = getTransactions(forMonth: date)
        
        let income = transactions
            .filter { $0.type == .income }
            .reduce(0) { $0 + $1.amount }
        
        let expense = transactions
            .filter { $0.type == .expense }
            .reduce(0) { $0 + $1.amount }
        
        let balance = income - expense
        
        return (income, expense, balance)
    }
    
    func getCategoryStatistics(for date: Date) -> [CategoryStat] {
        let transactions = getTransactions(forMonth: date)
        var categoryStats: [Int: CategoryStat] = [:] // categoryId: CategoryStat
        
        // 获取所有类别
        let categories = getDefaultCategories()
        
        // 初始化统计
        for category in categories {
            categoryStats[category.id] = CategoryStat(category: category, amount: 0, count: 0)
        }
        
        // 计算统计
        for transaction in transactions where transaction.type == .expense {
            if var stat = categoryStats[transaction.category.id] {
                stat.amount += transaction.amount
                stat.count += 1
                categoryStats[transaction.category.id] = stat
            }
        }
        
        return Array(categoryStats.values)
            .filter { $0.amount > 0 }
            .sorted { $0.amount > $1.amount }
    }
    
    // MARK: - Category Management
    
    func getDefaultCategories() -> [Category] {
        return [
            Category(id: 1, name: "餐饮", icon: "🍽️"),
            Category(id: 2, name: "交通", icon: "🚗"),
            Category(id: 3, name: "购物", icon: "🛍️"),
            Category(id: 4, name: "娱乐", icon: "🎮"),
            Category(id: 5, name: "医疗", icon: "🏥"),
            Category(id: 6, name: "教育", icon: "📚"),
            Category(id: 7, name: "工资", icon: "💰"),
            Category(id: 8, name: "奖金", icon: "🏆"),
            Category(id: 9, name: "投资", icon: "📈"),
            Category(id: 10, name: "其他", icon: "📝")
        ]
    }
    
    // MARK: - Private Methods
    
    private func saveTransactions(_ transactions: [Transaction]) {
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(transactions)
            UserDefaults.standard.set(data, forKey: transactionsKey)
        } catch {
            print("Failed to encode transactions: \(error)")
        }
    }
    
    // MARK: - Data Export/Import
    
    func exportData() -> String? {
        let transactions = getAllTransactions()
        
        var csvString = "日期,类型,类别,金额,备注\n"
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        for transaction in transactions {
            let dateString = dateFormatter.string(from: transaction.date)
            let typeString = transaction.type == .income ? "收入" : "支出"
            let amountString = String(format: "%.2f", transaction.amount)
            
            let row = "\"\(dateString)\",\"\(typeString)\",\"\(transaction.category.name)\",\"\(amountString)\",\"\(transaction.note)\"\n"
            csvString += row
        }
        
        return csvString
    }
    
    func importData(from csvString: String) -> Bool {
        let lines = csvString.components(separatedBy: .newlines)
        guard lines.count > 1 else { return false } // 至少要有标题行和数据行
        
        var importedTransactions: [Transaction] = []
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        
        for (index, line) in lines.enumerated() {
            if index == 0 { continue } // 跳过标题行
            if line.isEmpty { continue }
            
            let components = parseCSVLine(line)
            guard components.count >= 5 else { continue }
            
            let dateString = components[0]
            let typeString = components[1]
            let categoryName = components[2]
            let amountString = components[3]
            let note = components[4]
            
            guard let date = dateFormatter.date(from: dateString),
                  let amount = Double(amountString),
                  amount > 0 else {
                continue
            }
            
            let type: TransactionType = typeString == "收入" ? .income : .expense
            let category = getDefaultCategories().first { $0.name == categoryName } ?? Category(id: 10, name: "其他", icon: "📝")
            
            let transaction = Transaction(
                id: UUID(),
                amount: amount,
                category: category,
                date: date,
                note: note,
                type: type
            )
            
            importedTransactions.append(transaction)
        }
        
        if !importedTransactions.isEmpty {
            let existingTransactions = getAllTransactions()
            let allTransactions = existingTransactions + importedTransactions
            saveTransactions(allTransactions)
            return true
        }
        
        return false
    }
    
    private func parseCSVLine(_ line: String) -> [String] {
        var result: [String] = []
        var currentField = ""
        var inQuotes = false
        
        for char in line {
            if char == "\"" {
                inQuotes.toggle()
            } else if char == "," && !inQuotes {
                result.append(currentField)
                currentField = ""
            } else {
                currentField.append(char)
            }
        }
        
        result.append(currentField)
        return result
    }
}

// 统计数据结构
struct CategoryStat {
    let category: Category
    var amount: Double
    var count: Int
}

// 扩展Transaction使其可编码
struct Transaction: Codable {
    let id: UUID
    let amount: Double
    let category: Category
    let date: Date
    let note: String
    let type: TransactionType
}

enum TransactionType: String, Codable {
    case income
    case expense
}

struct Category: Codable {
    let id: Int
    let name: String
    let icon: String
}

// 扩展Category使其可比较
extension Category: Equatable {
    static func == (lhs: Category, rhs: Category) -> Bool {
        return lhs.id == rhs.id
    }
}

// 扩展Date使其可编码
extension Date {
    func toISOString() -> String {
        let formatter = ISO8601DateFormatter()
        return formatter.string(from: self)
    }
    
    static func fromISOString(_ string: String) -> Date? {
        let formatter = ISO8601DateFormatter()
        return formatter.date(from: string)
    }
}