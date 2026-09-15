from isbn13 import *

if __name__ == "__main__":
    assert check_isbn13(9783161484100) == True
    assert check_isbn13(9783361484100) == False
    assert check_isbn13(1134685992) == False
    assert check_isbn13(9780321356680) == True
    assert check_isbn13(97802016162241) == False

    assert make_isbn13(978013468599) == 9780134685991
    assert make_isbn13(978149204034) == 9781492040347
    assert make_isbn13(0) == 0
    assert make_isbn13(4) == 42

    print("Passed all tests")