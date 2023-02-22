-- Sha-key

ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDqDf7jSE4M7yGMHNTovgqwbmg/1/QP6nAEXwij3ewKQDTG7of2ie8Z7IyIww7UP9nv/wLNKUYIannsrLvupIhXPD5jIjeR45G1XdM/0JRt3jBoIxXI3sge9/ZUC/XbyLuta5PYc2Sp7oUrglAMcgI9J6xi1k/qIbzFqxDm6HsaovN9sHmwJlELUNYuB0z86P+jkq8vWr3cVQCnYV60Ww3fY0e6rwHcCQRfCrNAVGWt2FIUr01Gc+HlQWnk9YZiYd40ML9xZVZTw22pVph47vtey6UGbCy9VAkF/ZJ/pW8zKml12vJrbdLPrfb4nme5XmYjq2Cueo+d6hezvQV69759dLXUDMuvpMISMwkVo7vjVk6naxbAjbgCJYLp8GraBLCyFy9TEiBYaJCZ2/R29vcoVAxpKjSBP4muSjSNOZK44KyV+O8uCjEcpmxXQ0rlPAoQsaZaytquCG0d5GdnS3lkwAM9MpQQFYDAfIR89M7cLEd2UGoqw+nsCJOL0zMkEc0= G14-T1D

-- Server run as 

export GOOGLE_APPLICATION_CREDENTIALS="/home/G14-T1D/key.json"
sudo java -jar /home/G14-T1D/Server.jar -p 80

-- Worker run as

export GOOGLE_APPLICATION_CREDENTIALS="/home/G14-T1D/key.json"
sudo java -jar /home/G14-T1D/VisionWorker.jar



