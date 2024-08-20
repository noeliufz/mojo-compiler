	.text
PrintInt.CString:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.8:
#	movq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je PrintInt.CString.badPtr
L.3:
	movq %rbx,%rcx
	xorq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl PrintInt.CString.badSub
L.4:
	movq 8(%rcx),%rax
	cmpq %rax,%rbx
	jge PrintInt.CString.badSub
L.5:
	movq 0(%rcx),%rax
#	movq %rax,%rax
	addq %rbx,%rax
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
PrintInt.CString.badPtr:
	call badPtr
PrintInt.CString.badSub:
	call badSub
	.data
	.balign 8
L.6:
	.byte 78
	.byte 117
	.byte 109
	.byte 98
	.byte 101
	.byte 114
	.byte 32
	.byte 105
	.byte 115
	.byte 58
	.byte 32
	.byte 37
	.byte 100
	.byte 10
	.byte 0
	.data
	.balign 8
L.7:
	.quad L.6
	.quad 15
	.text
PrintInt:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.9:
	leaq L.7(%rip),%rax
	movq %rax,%rdi
	call PrintInt.CString
#	movq %rax,%rax
	movq %rax,%rdi
	movq $42,%rax
	movq %rax,%rsi
	call printf
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
PrintInt.badSub:
	call badSub
