#!/usr/bin/perl

my %port = get_port();
# my @names = ('Shuai Ni', 'Yifu Sun', 'Zhi Xu', 'Yang Tian', 'Wenji Liu', 'Shuai Hu', 'Fangshi Li', 'Feiyi Wang', 'Oulu Xu', 'Jing Li');
my @names = ('Shuai Ni', 'Yifu Sun', 'Zhi Xu', 'Yang Tian');
my $names_len = scalar @names;
my $msgCount = 100;


# Total message sent is $msgCount x Combination(n, 2)
for (my $i = 0; $i < $names_len; $i += 1) {
	for (my $j = $i + 1; $j < $names_len; $j += 1) {
		# print "$i, $j\n";
		system qq{mvn exec:java -D exec.mainClass="com.Elessar.app.PerfTestMain" -D exec.args="'$names[$i]' $port{$names[$i]} '$names[$j]' $port{$names[$j]} $msgCount" &};
	}
}

sub get_port {
	return (
		"Shuai Ni" => 2000,
		"Yifu Sun" => 2500,
		"Zhi Xu" => 3000,
		"Yang Tian" => 3500,
		"Wenji Liu" => 4000,
		"Shuai Hu" => 4500,
		"Fangshi Li" => 5000,
		"Feiyi Wang" => 5500,
		"Oulu Xu" => 6000,
		"Jing Li" => 6500
	);
}
