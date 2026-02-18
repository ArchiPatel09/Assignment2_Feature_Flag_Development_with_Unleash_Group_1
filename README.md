# PROG3360 Assignment 2: Feature Flag Development with Unleash
# Group: 01

This project builds on top of the e-commerce microservices system of Assignment 1 by adding feature flags, where Unleash is used as the feature flag management server. The system consists of two microservices (Product Service and Order Service) connected to Unleash to toggle features dynamically and containerized by Docker Compose and released by GitHub Actions CI/CD.

# Features implemented
- premium-pricing:
    Service: Product Service 
    Description: 10 percent discount on all products on premium users.

- order-notifications:
    Service: Order Service 
    Description: Simulated logging of email/SMS on order creation.
    
- bulk-order-discount: 
    Service: Order Service 
    Description: 15 percent discount on order quantity of more than 5 items.

How to run:
1. Clone the repository
2. Start the entire stack
    docker-compose up -d
3. Initialize feature flags
    Navigate to scripts folder in git bash
    chmod +x scripts/init-flags.sh
    ./init-flags.sh
4. Verify services are running
    docker-compose ps
5. Access Unleash UI
    URL: http://localhost:4242
    Username: admin
    Password: unleash4all
6. You can unable/disable the flags from Unleash UI and test them on postman. 
7. Run all unit test locally by clicking right and then click on run all test button.

GitHub repository link: https://github.com/ArchiPatel09/Assignment2_Feature_Flag_Development_with_Unleash_Group_1
YouTube video link: https://youtu.be/KCgvfbwLnHQ 