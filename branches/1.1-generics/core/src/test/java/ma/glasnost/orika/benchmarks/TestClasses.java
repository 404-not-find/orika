package ma.glasnost.orika.benchmarks;

public interface TestClasses {
    public static class Product {
        private String productName;
        private String productDescription;
        private Double price;
        private Boolean availability;
        public String getProductName() {
            return productName;
        }
        public void setProductName(String productName) {
            this.productName = productName;
        }
        public String getProductDescription() {
            return productDescription;
        }
        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }
        public Double getPrice() {
            return price;
        }
        public void setPrice(Double price) {
            this.price = price;
        }
        public Boolean getAvailability() {
            return availability;
        }
        public void setAvailability(Boolean availability) {
            this.availability = availability;
        } 
    }
    
    
    public static class ProductDto {
        private String productName;
        private String productDescription;
        private Double price;
        private Boolean availability;
        public String getProductName() {
            return productName;
        }
        public void setProductName(String productName) {
            this.productName = productName;
        }
        public String getProductDescription() {
            return productDescription;
        }
        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }
        public Double getPrice() {
            return price;
        }
        public void setPrice(Double price) {
            this.price = price;
        }
        public Boolean getAvailability() {
            return availability;
        }
        public void setAvailability(Boolean availability) {
            this.availability = availability;
        }   
    }
}
