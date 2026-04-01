# Online Auction System

## Project Overview
A comprehensive online auction system built with Java, featuring real-time bidding, auto-bidding, and anti-sniping protection.

## Team Members
|         Name        |    ID    | Contribution |
|---------------------|----------|--------------|
| Hoàng Phương Nhi    | 25023508 |  |
| Nguyễn Ngọc Quỳnh   | 25023524 |  |
| Đỗ Khánh Linh       | 25023488 | |
| Đặng Thị Phương Anh | 25023432 | |

## Documentation

### Class Diagram
![Class Diagram](docs/diagrams/class-diagram.png)

The class diagram above shows the complete object-oriented design of our auction system, including:
- **Entity Hierarchy**: Base classes for users and items
- **User Types**: Bidder, Seller, Admin with specific roles
- **Item Types**: Electronics, Art, Vehicle extending Item
- **Core Business**: Auction, BidTransaction with state management
- **Design Patterns**: Singleton, Factory, Observer
- **DAO Layer**: Data persistence with UserDAO, AuctionDAO


## Setup Instructions

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Git

### Clone Repository
```bash
