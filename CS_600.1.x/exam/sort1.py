#!/usr/bin/env python
def sort1(lst):
	swapFlag = True
	iteration = 0
	while swapFlag:
		swapFlag = False
		for i in range(len(lst)-1):
			if lst[i] > lst[i+1]:
				temp = lst[i+1]
				lst[i+1] = lst[i]
				lst[i] = temp
				swapFlag = True
		L = lst[:]  # the next 3 questions below refer to this line
		iteration += 1
		if iteration == 1:
			print lst, '\n', L
	return lst

sort1([4,3,2,1])
