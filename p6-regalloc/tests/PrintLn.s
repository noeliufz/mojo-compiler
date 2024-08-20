	.text
PrintLn.println:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rdi,%rax
L.4:
	movq %rax,t.0
	xorq %rax,%rax
	cmpq %rax,t.0
	je PrintLn.println.badPtr
L.1:
	movq 0(t.0),t.3
	movq t.3,%rdi
	call puts
	movq %rax,t.1
L.0:
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
PrintLn.println.badPtr:
	call badPtr
	.data
	.balign 8
L.2:
	.byte 72
	.byte 101
	.byte 108
	.byte 108
	.byte 111
	.byte 32
	.byte 87
	.byte 111
	.byte 114
	.byte 108
	.byte 100
	.byte -30
	.byte -100
	.byte -123
	.byte 0
	.data
	.balign 8
L.3:
	.quad L.2
	.quad 15
	.text
PrintLn:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.5:
	leaq L.3(%rip),t.5
	movq t.5,%rdi
	call PrintLn.println
	movq %rax,t.4
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
