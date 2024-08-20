#!/usr/bin/env python3
import glob
import random

def main():
    with open("all.tok") as fd:
        tokens = fd.readlines()
        print("".join([random.choice(tokens).strip() for _ in range(20)]))

            
if __name__ == "__main__":
    main()